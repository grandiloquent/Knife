package euphoria.psycho.knife;

import android.graphics.drawable.Drawable;

import euphoria.psycho.common.cache.LruCache;

public class MemoryCache {

    LruCache<String, Drawable> mLruCache;

    private MemoryCache() {
        mLruCache = new LruCache<>(200);

    }

    public void put(String path, Drawable drawable) {
        mLruCache.put(path, drawable);
    }

    public Drawable get(String path) {
        return mLruCache.get(path);
    }

    public static MemoryCache instance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final MemoryCache INSTANCE =
                new MemoryCache();
    }
}
