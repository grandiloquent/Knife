package euphoria.psycho.knife.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.AtomicFile;
import androidx.core.util.Pair;
import euphoria.psycho.common.ConversionUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.StreamUtil;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.task.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;

public class ThumbnailDiskStorage {
    private static final int MAX_CACHE_BYTES =
            5 * ConversionUtils.BYTES_PER_MEGABYTE; // Max disk cache size is 5MB.
    private static final String TAG = "TAG/" + ThumbnailDiskStorage.class.getSimpleName();
    static final LinkedHashSet<Pair<String, Integer>> sDiskLruCache =
            new LinkedHashSet<Pair<String, Integer>>();
    static final HashMap<String, HashSet<Integer>> sIconSizesMap =
            new HashMap<String, HashSet<Integer>>();
    private final int mMaxCacheBytes;
    final ThumbnailGenerator mThumbnailGenerator;
    private File mDirectory;
    private ThumbnailStorageDelegate mDelegate;
    long mSizeBytes;

    ThumbnailDiskStorage(ThumbnailStorageDelegate delegate, ThumbnailGenerator thumbnailGenerator,
                         int maxCacheSizeBytes) {
        ThreadUtils.assertOnUiThread();
        mDelegate = delegate;
        mThumbnailGenerator = thumbnailGenerator;
        mMaxCacheBytes = maxCacheSizeBytes;
        //new InitTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    void addToDisk(String contentId, Bitmap bitmap, int iconSizePx) {
        ThreadUtils.assertOnBackgroundThread();
        if (!isInitialized()) return;

        if (sDiskLruCache.contains(Pair.create(contentId, iconSizePx))) {
            removeFromDiskHelper(Pair.create(contentId, iconSizePx));
        }

        FileOutputStream fos = null;
        AtomicFile atomicFile = null;
        try {
            // Compress bitmap to PNG.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] compressedBitmapBytes = baos.toByteArray();

            // Construct proto.
//            ThumbnailEntry newEntry =
//                    ThumbnailEntry.newBuilder()
//                            .setContentId(ContentId.newBuilder().setId(contentId))
//                            .setSizePx(iconSizePx)
//                            .setCompressedPng(ByteString.copyFrom(compressedBitmapBytes))
//                            .build();

            // Write proto to disk.
            File newFile = new File(getThumbnailFilePath(contentId, iconSizePx));
            atomicFile = new AtomicFile(newFile);
            fos = atomicFile.startWrite();
//            fos.write(newEntry.toByteArray());
            atomicFile.finishWrite(fos);

            // Update internal cache state.
            sDiskLruCache.add(Pair.create(contentId, iconSizePx));
            if (sIconSizesMap.containsKey(contentId)) {
                sIconSizesMap.get(contentId).add(iconSizePx);
            } else {
                HashSet<Integer> iconSizes = new HashSet<Integer>();
                iconSizes.add(iconSizePx);
                sIconSizesMap.put(contentId, iconSizes);
            }
            mSizeBytes += newFile.length();

            trim();
        } catch (IOException e) {
            Log.e(TAG, "Error while writing to disk.", e);
            atomicFile.failWrite(fos);
        }
    }

    void trim() {
        ThreadUtils.assertOnBackgroundThread();
        while (mSizeBytes > mMaxCacheBytes) {
            removeFromDiskHelper(sDiskLruCache.iterator().next());
        }
    }

    public void destroy() {
        mThumbnailGenerator.destroy();
    }

    Bitmap getFromDisk(String contentId, int iconSizePx) {
        ThreadUtils.assertOnBackgroundThread();
        if (!isInitialized()) return null;

        if (!sDiskLruCache.contains(Pair.create(contentId, iconSizePx))) return null;

        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            String thumbnailFilePath = getThumbnailFilePath(contentId, iconSizePx);
            File file = new File(thumbnailFilePath);
            // If file doesn't exist, {@link mSizeBytes} cannot be updated to account for the
            // removal but this is fine in the long-run when trim happens.
            if (!file.exists()) return null;

            AtomicFile atomicFile = new AtomicFile(file);
            fis = atomicFile.openRead();
//            ThumbnailEntry entry = ThumbnailEntry.parseFrom(atomicFile.readFully());
//            if (!entry.hasCompressedPng()) return null;
//
//            bitmap = BitmapFactory.decodeByteArray(
//                    entry.getCompressedPng().toByteArray(), 0, entry.getCompressedPng().size());
        } catch (IOException e) {
            Log.e(TAG, "Error while reading from disk.", e);
        } finally {
            StreamUtil.closeQuietly(fis);
        }

        return bitmap;
    }

    private String getThumbnailFilePath(String contentId, int iconSizePx) {
        return mDirectory.getPath() + File.separator + contentId + iconSizePx + ".entry";
    }

    private boolean isInitialized() {
        return mDirectory != null;
    }

    public void onThumbnailRetrieved(
            @NonNull String contentId, @Nullable Bitmap bitmap, int iconSizePx) {
        ThreadUtils.assertOnUiThread();
        if (bitmap != null && !TextUtils.isEmpty(contentId)) {
            new CacheThumbnailTask(contentId, bitmap, iconSizePx)
                    .executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }
        mDelegate.onThumbnailRetrieved(contentId, bitmap);
    }

    public void removeFromDisk(String contentId) {
        ThreadUtils.assertOnUiThread();
        if (!isInitialized()) return;

        if (!sIconSizesMap.containsKey(contentId)) return;

        // new RemoveThumbnailTask(contentId).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    void removeFromDiskHelper(Pair<String, Integer> contentIdSizePair) {
        ThreadUtils.assertOnBackgroundThread();

        String contentId = contentIdSizePair.first;
        int iconSizePx = contentIdSizePair.second;
        File file = new File(getThumbnailFilePath(contentId, iconSizePx));
        if (!file.exists()) {
            Log.e(TAG, "Error while removing from disk. File does not exist.");
            return;
        }

        long fileSizeBytes = 0;
        try {
            fileSizeBytes = file.length();
        } catch (SecurityException se) {
            Log.e(TAG, "Error while removing from disk. File denied read access.", se);
        }
        AtomicFile atomicFile = new AtomicFile(file);
        atomicFile.delete();

        // Update internal cache state.
        sDiskLruCache.remove(contentIdSizePair);
        sIconSizesMap.get(contentId).remove(iconSizePx);
        if (sIconSizesMap.get(contentId).size() == 0) {
            sIconSizesMap.remove(contentId);
        }
        mSizeBytes -= fileSizeBytes;
    }

    public void retrieveThumbnail(ThumbnailProvider.ThumbnailRequest request) {
        ThreadUtils.assertOnUiThread();
        if (TextUtils.isEmpty(request.getContentId())) return;

        new GetThumbnailTask(request).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    public static ThumbnailDiskStorage create(ThumbnailStorageDelegate delegate) {
        return new ThumbnailDiskStorage(delegate, new ThumbnailGenerator(), MAX_CACHE_BYTES);
    }

    class CacheThumbnailTask extends AsyncTask<Void> {
        private final Bitmap mBitmap;
        private final String mContentId;
        private final int mIconSizePx;

        public CacheThumbnailTask(String contentId, Bitmap bitmap, int iconSizePx) {
            mContentId = contentId;
            mBitmap = bitmap;
            mIconSizePx = iconSizePx;
        }

        @Override
        protected Void doInBackground() {
            addToDisk(mContentId, mBitmap, mIconSizePx);
            return null;
        }
    }

    private class GetThumbnailTask extends AsyncTask<Bitmap> {
        private final ThumbnailProvider.ThumbnailRequest mRequest;

        public GetThumbnailTask(ThumbnailProvider.ThumbnailRequest request) {
            mRequest = request;
        }

        @Override
        protected Bitmap doInBackground() {
            if (sDiskLruCache.contains(
                    Pair.create(mRequest.getContentId(), mRequest.getIconSize()))) {
                return getFromDisk(mRequest.getContentId(), mRequest.getIconSize());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                onThumbnailRetrieved(mRequest.getContentId(), bitmap, mRequest.getIconSize());
                return;
            }
            // Asynchronously process the file to make a thumbnail.
            mThumbnailGenerator.retrieveThumbnail(mRequest, ThumbnailDiskStorage.this);
        }
    }
}
