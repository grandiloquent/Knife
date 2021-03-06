package euphoria.psycho.download;

import java.util.concurrent.BlockingQueue;

public class CacheDispatcher extends Thread {

    private final BlockingQueue<Request<?>> mCacheQueue;
    private final BlockingQueue<Request<?>> mNetworkQueue;
    private final Cache mCache;
    private final ResponseDelivery mDelivery;

    private volatile boolean mQuit = false;
    private final WaitingRequestManager mWaitingRequestManager;

    public CacheDispatcher(BlockingQueue<Request<?>> cacheQueue,
                           BlockingQueue<Request<?>> networkQueue,
                           Cache cache,
                           ResponseDelivery delivery) {
        mCacheQueue = cacheQueue;
        mNetworkQueue = networkQueue;
        mCache = cache;
        mDelivery = delivery;
        mWaitingRequestManager = new WaitingRequestManager(this);
    }

    @Override
    public void run() {
        super.run();
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }
}
