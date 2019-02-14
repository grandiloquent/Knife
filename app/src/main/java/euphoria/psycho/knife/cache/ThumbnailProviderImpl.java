package euphoria.psycho.knife.cache;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LruCache;
import euphoria.psycho.common.ConversionUtils;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.pool.DiscardableReferencePool;

/**
 * Concrete implementation of {@link ThumbnailProvider}.
 *
 * Thumbnails are cached in {@link BitmapCache}. The cache key is a pair of the filepath and
 * the height/width of the thumbnail. Value is the thumbnail.
 *
 * A queue of requests is maintained in FIFO order.
 *
 * TODO(dfalcantara): Figure out how to send requests simultaneously to the utility process without
 *                    duplicating work to decode the same image for two different requests.
 */
public class ThumbnailProviderImpl implements ThumbnailProvider, ThumbnailStorageDelegate {
    /** Default in-memory thumbnail cache size. */
    private static final int DEFAULT_MAX_CACHE_BYTES = 5 * ConversionUtils.BYTES_PER_MEGABYTE;

    /**
     * Helper object to store in the LruCache when we don't really need a value but can't use null.
     */
    private static final Object NO_BITMAP_PLACEHOLDER = new Object();

    /**
     * An in-memory LRU cache used to cache bitmaps, mostly improve performance for scrolling, when
     * the view is recycled and needs a new thumbnail.
     */
    private BitmapCache mBitmapCache;

    /**
     * Tracks a set of Content Ids where thumbnail generation or retrieval failed.  This should
     * prevent making subsequent (potentially expensive) thumbnail generation requests when there
     * would be no point.
     */
    private LruCache<String /* Content Id */, Object /* Placeholder */> mNoBitmapCache =
            new LruCache<>(100);

    /** Queue of files to retrieve thumbnails for. */
    private final Deque<ThumbnailRequest> mRequestQueue = new ArrayDeque<>();

    /** Request that is currently having its thumbnail retrieved. */
    private ThumbnailRequest mCurrentRequest;

    private ThumbnailDiskStorage mStorage;

    /**
     * Constructor to build the thumbnail provider with default thumbnail cache size.
     * @param referencePool The application's reference pool.
     */
    public ThumbnailProviderImpl(DiscardableReferencePool referencePool) {
        this(referencePool, DEFAULT_MAX_CACHE_BYTES);
    }

    /**
     * Constructor to build the thumbnail provider.
     * @param referencePool The application's reference pool.
     * @param bitmapCacheSizeByte The size in bytes of the in-memory LRU bitmap cache.
     */
    public ThumbnailProviderImpl(DiscardableReferencePool referencePool, int bitmapCacheSizeByte) {
        ThreadUtils.assertOnUiThread();
        mBitmapCache = new BitmapCache(referencePool, bitmapCacheSizeByte);
        mStorage = ThumbnailDiskStorage.create(this);
    }

    @Override
    public void destroy() {
        ThreadUtils.assertOnUiThread();
        mStorage.destroy();
    }

    /**
     * The returned bitmap will have at least one of its dimensions smaller than or equal to the
     * size specified in the request. Requests with no file path or content ID will not be
     * processed.
     *
     * @param request Parameters that describe the thumbnail being retrieved.
     */
    @Override
    public void getThumbnail(ThumbnailRequest request) {
        ThreadUtils.assertOnUiThread();

        if (TextUtils.isEmpty(request.getContentId())) {
            return;
        }

        if (mNoBitmapCache.get(request.getContentId()) != null) {
            request.onThumbnailRetrieved(request.getContentId(), null);
            return;
        }

        Bitmap cachedBitmap = getBitmapFromCache(request.getContentId(), request.getIconSize());
        if (cachedBitmap != null) {
            request.onThumbnailRetrieved(request.getContentId(), cachedBitmap);
            return;
        }

        mRequestQueue.offer(request);
        processQueue();
    }

    /** Removes a particular file from the pending queue. */
    @Override
    public void cancelRetrieval(ThumbnailRequest request) {
        ThreadUtils.assertOnUiThread();
        if (mRequestQueue.contains(request)) mRequestQueue.remove(request);
    }

    /**
     * Removes the thumbnails (different sizes) with {@code contentId} from disk.
     * @param contentId The content ID of the thumbnail to remove.
     */
    @Override
    public void removeThumbnailsFromDisk(String contentId) {
        mStorage.removeFromDisk(contentId);
    }

    private void processQueue() {
        ThreadUtils.postOnUiThread(this::processNextRequest);
    }

    private String getKey(String contentId, int bitmapSizePx) {
        return String.format(Locale.US, "id=%s, size=%d", contentId, bitmapSizePx);
    }

    private Bitmap getBitmapFromCache(String contentId, int bitmapSizePx) {
        String key = getKey(contentId, bitmapSizePx);
        Bitmap cachedBitmap = mBitmapCache.getBitmap(key);
        assert cachedBitmap == null || !cachedBitmap.isRecycled();
        return cachedBitmap;
    }

    private void processNextRequest() {
        ThreadUtils.assertOnUiThread();
        if (mCurrentRequest != null) return;
        if (mRequestQueue.isEmpty()) return;

        mCurrentRequest = mRequestQueue.poll();

        Bitmap cachedBitmap =
                getBitmapFromCache(mCurrentRequest.getContentId(), mCurrentRequest.getIconSize());
        if (cachedBitmap == null) {
            handleCacheMiss(mCurrentRequest);
        } else {
            // Send back the already-processed file.
            onThumbnailRetrieved(mCurrentRequest.getContentId(), cachedBitmap);
        }
    }

    /**
     * In the event of a cache miss from the in-memory cache, the thumbnail request is routed to one
     * of the following :
     * 1. May be the thumbnail request can directly provide the thumbnail.
     * 2. Otherwise, the request is sent to {@link ThumbnailDiskStorage} which is a disk cache. If
     * not found in disk cache, it would request the {@link ThumbnailGenerator} to generate a new
     * thumbnail for the given file path.
     * @param request Parameters that describe the thumbnail being retrieved
     */
    private void handleCacheMiss(ThumbnailRequest request) {
        boolean providedByThumbnailRequest = request.getThumbnail(
                bitmap -> onThumbnailRetrieved(request.getContentId(), bitmap));

        if (!providedByThumbnailRequest) {
            // Asynchronously process the file to make a thumbnail.
            assert !TextUtils.isEmpty(request.getFilePath());
            mStorage.retrieveThumbnail(request);
        }
    }

    /**
     * Called when thumbnail is ready, retrieved from memory cache or by
     * {@link ThumbnailDiskStorage} or by {@link ThumbnailRequest#getThumbnail}.
     * @param contentId Content ID for the thumbnail retrieved.
     * @param bitmap The thumbnail retrieved.
     */
    @Override
    public void onThumbnailRetrieved(@NonNull String contentId, @Nullable Bitmap bitmap) {
        if (bitmap != null) {
            // The bitmap returned here is retrieved from the native side. The image decoder there
            // scales down the image (if it is too big) so that one of its sides is smaller than or
            // equal to the required size. We check here that the returned image satisfies this
            // criteria.
            assert Math.min(bitmap.getWidth(), bitmap.getHeight()) <= mCurrentRequest.getIconSize();
            assert TextUtils.equals(mCurrentRequest.getContentId(), contentId);

            // We set the key pair to contain the required size (maximum dimension (pixel) of the
            // smaller side) instead of the minimal dimension of the thumbnail so that future
            // fetches of this thumbnail can recognise the key in the cache.
            String key = getKey(contentId, mCurrentRequest.getIconSize());
            mBitmapCache.putBitmap(key, bitmap);
            mNoBitmapCache.remove(contentId);
            mCurrentRequest.onThumbnailRetrieved(contentId, bitmap);
        } else {
            mNoBitmapCache.put(contentId, NO_BITMAP_PLACEHOLDER);
            mCurrentRequest.onThumbnailRetrieved(contentId, null);
        }

        mCurrentRequest = null;
        processQueue();
    }
}
