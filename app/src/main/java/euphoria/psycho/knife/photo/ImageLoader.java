package euphoria.psycho.knife.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import euphoria.psycho.common.Log;

public class ImageLoader {

    private ExecutorService mService;
    private Context mContext;
    private final int mMaxSize;
    private Handler mHandler = new Handler();


    public ImageLoader(Context context, int maxSize) {
        mService = Executors.newSingleThreadExecutor();
        mContext = context;
        mMaxSize = maxSize;
    }

    public void loadImage(String path, ImageLoaderObserver loaderObserver) {
        if (path == null) {
            loaderObserver.onLoadFinished(null);
            return;
        }
        mService.submit(() -> {

            Log.e("TAG/ImageLoader", "loadImage: " + path);

            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Drawable drawable = null;
            if (bitmap != null) {
                drawable = new BitmapDrawable(mContext.getResources(), bitmap);
            }
            final Drawable finalDrawable = drawable;
            mHandler.post(() -> {
                loaderObserver.onLoadFinished(finalDrawable);
            });
        });
    }
}
