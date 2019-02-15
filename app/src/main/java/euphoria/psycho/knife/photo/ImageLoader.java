package euphoria.psycho.knife.photo;

import android.content.Context;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import euphoria.psycho.common.Utils;

public class ImageLoader {

    private ExecutorService mService;
    private Context mContext;
    private final int mMaxSize;
    private Map<String, ImageLoaderJob> mJobs = new HashMap<>();


    public ImageLoader(Context context, int maxSize) {
        mService = Executors.newSingleThreadExecutor();
        mContext = context;
        mMaxSize = maxSize;
    }

    public void loadImage(String path, String id, ImageLoaderObserver loaderObserver) {
        if (path == null) {
            loaderObserver.onLoadFinished(null);
            return;
        }



            ImageLoaderJob job = new ImageLoaderJob(mContext, id, path, mMaxSize, loaderObserver);

            mService.submit(job);

    }

    public void removeJob(String id) {

    }
}
