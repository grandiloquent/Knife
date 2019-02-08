package euphoria.psycho.knife;

import euphoria.psycho.common.ContextUtils;
import euphoria.psycho.common.base.BaseApp;
import euphoria.psycho.common.pool.BytesBufferPool;

public class App extends BaseApp {

    private static final int BYTESBUFFER_SIZE = 200 * 1024;
    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static BytesBufferPool sBytesBufferPool;
    private static ImageCacheService sImageCacheService;

    @Override
    public void onCreate() {
        super.onCreate();
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread t, Throwable e) {
//                e.printStackTrace();
//                Log.e("TAG/", e.getMessage());
//            }
//        });
    }

    public static BytesBufferPool getBytesBufferPool() {
        if (sBytesBufferPool == null) {
            sBytesBufferPool = new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);
        }
        return sBytesBufferPool;
    }


    public static ImageCacheService getImageCacheService() {
        if (sImageCacheService == null) {
            sImageCacheService = new ImageCacheService(ContextUtils.getApplicationContext());
        }
        return sImageCacheService;
    }
}
