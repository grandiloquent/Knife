package euphoria.psycho.knife.cache;

import android.graphics.Bitmap;

import euphoria.psycho.common.BitmapUtils;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.knife.cache.ThumbnailProvider.ThumbnailRequest;

public class ThumbnailGenerator {
    public void destroy() {
    }


    public void retrieveThumbnail(ThumbnailRequest request, ThumbnailGeneratorCallback callback) {
        Bitmap thumbnail = null;
        if (FileUtils.isSupportedVideo(request.getFilePath())) {


            thumbnail = createVideoThumbnail(request.getFilePath(), request.getIconSize());
        }

//        Log.e("TAG/ThumbnailGenerator", "retrieveThumbnail: "
//                + "\n thumbnail is null = " + (thumbnail == null)
//                + "\n filepath = " + request.getFilePath()
//                + "\n isVideo = " + FileUtils.isSupportedVideo(request.getFilePath()));


        callback.onThumbnailRetrieved(request.getContentId(),
                thumbnail,
                thumbnail == null ? 0 : thumbnail.getWidth());

    }

    private Bitmap createVideoThumbnail(String filePath, int iconSize) {
        Bitmap thumbnail = BitmapUtils.createVideoThumbnail(filePath);

        if (thumbnail == null) return null;
        if (thumbnail.getWidth() <= iconSize) return thumbnail;
        float scale = iconSize*1.0f / thumbnail.getWidth()*1.0f;


//        Log.e("TAG/ThumbnailGenerator", "createVideoThumbnail: "
//                + "\n thumbnail width = " + thumbnail.getWidth()
//                + "\n thumbnail height = " + thumbnail.getHeight()
//                + "\n iconSize = " + iconSize
//                + "\n scale = " + scale);

        return BitmapUtils.resizeBitmapByScale(thumbnail, scale, true);

    }
}
