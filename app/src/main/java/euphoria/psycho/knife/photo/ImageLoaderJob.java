package euphoria.psycho.knife.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Process;

public class ImageLoaderJob implements Runnable {

    private final String mId;
    private final String mImagePath;
    private final ImageLoaderObserver mObserver;
    private volatile boolean mIsStop;
    private final Context mContext;
    private final int mMaxSize;

    public ImageLoaderJob(Context context, String id, String imagePath, int maxSize, ImageLoaderObserver observer) {
        mImagePath = imagePath;
        mId = id;
        mObserver = observer;
        mContext = context;
        mMaxSize = maxSize;
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

        Bitmap bitmap = BitmapFactory.decodeFile(mImagePath);
        if (bitmap == null) {
            if (mObserver != null) mObserver.onLoadFinished(null);
        }

        Drawable drawable = new BitmapDrawable(mContext.getResources(), bitmap);
        mObserver.onLoadFinished(drawable);
    }
}
