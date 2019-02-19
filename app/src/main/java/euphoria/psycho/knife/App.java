package euphoria.psycho.knife;

import android.provider.ContactsContract.Directory;

import euphoria.psycho.common.base.BaseApp;
import euphoria.psycho.common.log.FileLogger;
import euphoria.psycho.common.pool.BytesBufferPool;
import euphoria.psycho.common.pool.DiscardableReferencePool;
import euphoria.psycho.share.util.ThreadUtils;

public class App extends BaseApp {

    private static final int BYTESBUFFER_SIZE = 200 * 1024;
    private static final int BYTESBUFFE_POOL_SIZE = 4;
    private static Status mStatus;
    private static BytesBufferPool sBytesBufferPool;
    private DiscardableReferencePool mReferencePool;

    public DiscardableReferencePool getReferencePool() {
        ThreadUtils.assertOnUiThread();
        if (mReferencePool == null) {
            mReferencePool = new DiscardableReferencePool();
        }
        return mReferencePool;
    }

    public static BytesBufferPool getBytesBufferPool() {
        if (sBytesBufferPool == null) {
            sBytesBufferPool = new BytesBufferPool(BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);
        }
        return sBytesBufferPool;
    }



    public static Status getmStatus() {
        return mStatus;
    }

    public static void setmStatus(Status mStatus) {
        App.mStatus = mStatus;
    }

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
        FileLogger.getLogger(getApplicationContext());
    }

    public static class Status {
        private Directory mDirectory;
        private int mScrollX;

        public Directory getDirectory() {
            return mDirectory;
        }

        public void setDirectory(Directory directory) {
            mDirectory = directory;
        }

        public int getScrollX() {
            return mScrollX;
        }

        public void setScrollX(int scrollX) {
            mScrollX = scrollX;
        }
    }

}
