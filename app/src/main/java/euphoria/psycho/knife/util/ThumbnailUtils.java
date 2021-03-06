package euphoria.psycho.knife.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Process;
import android.util.Pair;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import euphoria.common.Files;
import euphoria.psycho.common.Callback;
import euphoria.psycho.share.util.BitmapUtils;
import euphoria.psycho.share.util.IconUtils;

public class ThumbnailUtils {

    public interface ThumbnailProvider {
        void cancelRetrieval(ThumbnailRequest request);

        void destroy();

        void getThumbnail(ThumbnailRequest request);

        void removeThumbnailsFromDisk(String contentId);
    }

    public interface ThumbnailRequest {

        @Nullable
        String getContentId();

        @Nullable
        String getFilePath();

        int getIconSize();

        default boolean
        getThumbnail(Callback<Bitmap> callback) {
            return false;
        }

        void onThumbnailRetrieved(@NonNull String contentId, @Nullable Bitmap thumbnail);
    }

    private static class ImageLoader extends Thread {
        private ThumbnailRequest mRequest;

        private volatile boolean mIsStop;
        private String mCacheDirectory;
        private CopyOnWriteArrayList<Pair<String, ImageLoader>> mTasks;

        public ImageLoader(String cacheDirectory, ThumbnailRequest request, CopyOnWriteArrayList<Pair<String, ImageLoader>> tasks) {
            mCacheDirectory = cacheDirectory;
            mRequest = request;
            mTasks = tasks;
        }

        private Bitmap createImageThumbnail(String filePath, int iconSize) {
            Bitmap thumbnail = BitmapFactory.decodeFile(filePath);
            return BitmapUtils.resizeAndCropCenter(thumbnail, iconSize, true);

        }

        private Bitmap createVideoThumbnail(String filePath, int iconSize) {
            Bitmap thumbnail = BitmapUtils.createVideoThumbnail(filePath);

            if (thumbnail == null) return null;
            if (thumbnail.getWidth() <= iconSize) return thumbnail;

            return BitmapUtils.resizeAndCropCenter(thumbnail, iconSize, true);

        }

        private void remove() {
            int index = -1;
            String contentId = mRequest.getContentId();
            for (int i = 0, j = mTasks.size(); i < j; i++) {
                if (mTasks.get(i).first.equals(contentId)) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                mTasks.remove(index);
            }
        }

        public void stopLoad() {
            mIsStop = false;
        }

        @Override
        public void run() {


            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            if (mIsStop) {
                mRequest.onThumbnailRetrieved(mRequest.getContentId(), null);
                remove();
                return;
            }
            File destination = new File(mCacheDirectory, mRequest.getContentId());
            if (destination.exists()) {
                mRequest.onThumbnailRetrieved(mRequest.getContentId(), BitmapFactory.decodeFile(destination.getAbsolutePath()));
                remove();
                return;
            }
            Bitmap thumbnail = null;
            if (Files.isSupportedVideo(mRequest.getFilePath())) {


                thumbnail = createVideoThumbnail(mRequest.getFilePath(), mRequest.getIconSize());
            } else if (Files.isSupportedImage(mRequest.getFilePath())) {
                thumbnail = createImageThumbnail(mRequest.getFilePath(), mRequest.getIconSize());

            } else if (mRequest.getFilePath().endsWith(".apk")) {
                thumbnail = IconUtils.drawableToBitmap(IconUtils.getAppIcon(mRequest.getFilePath()));
            }
            if (thumbnail != null)
                BitmapUtils.saveAsPng(thumbnail, destination.getPath());
            mRequest.onThumbnailRetrieved(mRequest.getContentId(), thumbnail);
            remove();
        }
    }

    public static class ThumbnailProviderImpl implements ThumbnailProvider {
        Executor mExecutor;
        private String mCacheDirectory;
        private CopyOnWriteArrayList<Pair<String, ImageLoader>> mTasks = new CopyOnWriteArrayList<>();


        public ThumbnailProviderImpl() {
            File dir = new File(Environment.getExternalStorageDirectory(), ".thumbnail");
            dir.mkdirs();
            mCacheDirectory = dir.getAbsolutePath();

            mExecutor = Executors.newFixedThreadPool(5);
        }

        @Override
        public void cancelRetrieval(ThumbnailRequest request) {
            for (Pair<String, ImageLoader> task : mTasks) {
                if (task.first.equals(request.getContentId())) {
                    task.second.stopLoad();
                }
            }
        }

        @Override
        public void destroy() {

        }

        @Override
        public void getThumbnail(ThumbnailRequest request) {
            ImageLoader imageLoader = new ImageLoader(mCacheDirectory, request, mTasks);
            mTasks.add(Pair.create(request.getContentId(), imageLoader));

            mExecutor.execute(imageLoader);
        }

        @Override
        public void removeThumbnailsFromDisk(String contentId) {
            File destination = new File(mCacheDirectory, contentId);
            if (destination.exists()) destination.delete();
        }
    }
}
