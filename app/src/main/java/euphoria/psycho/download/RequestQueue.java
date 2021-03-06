package euphoria.psycho.download;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestQueue {


    public interface RequestEvent {
        public static final int REQUEST_QUEUED = 0;
        public static final int REQUEST_CACHE_LOOKUP_STARTED = 1;
        public static final int REQUEST_CACHE_LOOKUP_FINISHED = 2;
        public static final int REQUEST_NETWORK_DISPATCH_STARTED = 3;
        public static final int REQUEST_NETWORK_DISPATCH_FINISHED = 4;
        public static final int REQUEST_FINISHED = 5;
    }

    public interface RequestEventListener {
        void onRequestEvent(Request<?> request, int event);
    }

    private final AtomicInteger mSequenceGenerator = new AtomicInteger();

    private final Set<Request<?>> mCurrentRequests = new HashSet<>();
    private final PriorityBlockingQueue<Request<?>> mCacheQueue = new PriorityBlockingQueue<>();
    private final PriorityBlockingQueue<Request<?>> mNetworkQueue = new PriorityBlockingQueue<>();
    private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;
    private final Cache mCache;
    private final Network mNetwork;
    private final ResponseDelivery mDelivery;
    private final NetworkDispatcher[] mDispatchers;
    private CacheDispatcher mCacheDispatcher;
    private final List<RequestEventListener> mEventListeners = new ArrayList<>();


    public RequestQueue(Cache cache, Network network, int threadPoolSize, ResponseDelivery delivery) {
        mCache = cache;
        mNetwork = network;
        mDispatchers = new NetworkDispatcher[threadPoolSize];
        mDelivery = delivery;
    }
//
//    public RequestQueue(Cache cache, Network network, int threadPoolSize) {
//
//    }
//
//    public RequestQueue(Cache cache, Network network) {
//        this(cache, network, DEFAULT_NETWORK_THREAD_POOL_SIZE);
//    }

    public void start() {
        stop();
        mCacheDispatcher = new CacheDispatcher(mCacheQueue, mNetworkQueue, mCache, mDelivery);
        mCacheDispatcher.start();

        for (int i = 0; i < mDispatchers.length; i++) {
            NetworkDispatcher networkDispatcher = new NetworkDispatcher(mNetworkQueue, mNetwork, mCache, mDelivery);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    private void stop() {
        if (mCacheDispatcher != null) {
            mCacheDispatcher.quit();
        }
        for (final NetworkDispatcher mDispatcher : mDispatchers) {
            if (mDispatcher != null) {
                mDispatcher.quit();
            }
        }
    }

    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    public Cache getCache() {
        return mCache;
    }

    public interface RequestFilter {
        boolean apply(Request<?> request);
    }

    public void cancelAll(RequestFilter filter) {
        synchronized (mCurrentRequests) {
            for (Request<?> request : mCurrentRequests) {
                if (filter.apply(request)) {
                    request.cancel();
                }
            }
        }
    }

    public void cancelAll(final Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        }
        cancelAll(new RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return request.getTag() == tag;
            }
        });
    }

    public <T> Request<T> add(Request<T> request) {
        request.setRequestQueue(this);
        synchronized (mCurrentRequests) {
            mCurrentRequests.add(request);
        }
        request.setSequence(getSequenceNumber());
        request.addMarker("add-to-queue");
        sendRequestEvent(request, RequestEvent.REQUEST_QUEUED);
        if (!request.shouldCache()) {
            mNetworkQueue.add(request);
            return request;
        }
        mCacheQueue.add(request);
        return request;
    }

    <T> void finish(Request<T> request) {
        synchronized (mCurrentRequests) {
            mCurrentRequests.remove(request);
        }
        sendRequestEvent(request, RequestEvent.REQUEST_FINISHED);
    }

    void sendRequestEvent(Request<?> request, int event) {
        synchronized (mEventListeners) {
            for (RequestEventListener listener : mEventListeners) {
                listener.onRequestEvent(request, event);
            }
        }
    }
}
