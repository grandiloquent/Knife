package euphoria.psycho.knife.cache;

import euphoria.psycho.common.Log;
import euphoria.psycho.knife.cache.ThumbnailProvider.ThumbnailRequest;

public class ThumbnailGenerator {
    public void destroy() {
    }

    public void retrieveThumbnail(ThumbnailRequest request, ThumbnailDiskStorage thumbnailDiskStorage) {

        Log.e("TAG/ThumbnailGenerator", "retrieveThumbnail: " + request.getFilePath());

    }
}
