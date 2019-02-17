package euphoria.psycho.knife.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Process;

import euphoria.psycho.share.util.BitmapUtils;

public class ImageLoaderJob implements Runnable {

    private final String mId;
    private final String mImagePath;
    private final ImageLoaderObserver mObserver;
    private volatile boolean mIsStop;
    private final Context mContext;
    private final int mMaxNumPixels;

    public ImageLoaderJob(Context context, String id, String imagePath, int maxNumPixels, ImageLoaderObserver observer) {
        mImagePath = imagePath;
        mId = id;
        mObserver = observer;
        mContext = context;
        mMaxNumPixels = maxNumPixels;
    }

    public String getId() {
        return mId;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void stopJob() {
        mIsStop = true;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        if (mIsStop) {
            if (mObserver != null) mObserver.onLoadFinished(null);
            return;
        }

        int[] size = BitmapUtils.getBitmapSize(mImagePath);
        BitmapFactory.Options options = new Options();
        options.inSampleSize = BitmapUtils.computeSampleSize(size[0], size[1], BitmapUtils.UNCONSTRAINED, mMaxNumPixels);
        Bitmap bitmap = BitmapFactory.decodeFile(mImagePath);
        if (bitmap == null) {
            if (mObserver != null) mObserver.onLoadFinished(null);
        }

        Drawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        mObserver.onLoadFinished(drawable);
    }
}
