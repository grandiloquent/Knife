package euphoria.psycho.knife.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import euphoria.psycho.common.BitmapUtils;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.IconUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.knife.cache.ThumbnailProvider.ThumbnailRequest;

public class ThumbnailGenerator {
    public void destroy() {
    }


    public void retrieveThumbnail(ThumbnailRequest request, ThumbnailGeneratorCallback callback) {
        Bitmap thumbnail = null;
        if (FileUtils.isSupportedVideo(request.getFilePath())) {


            thumbnail = createVideoThumbnail(request.getFilePath(), request.getIconSize());
        } else if (FileUtils.isSupportedImage(request.getFilePath())) {
            thumbnail = createImageThumbnail(request.getFilePath(), request.getIconSize());

        } else if (request.getFilePath().endsWith(".apk")) {
            thumbnail = IconUtils.drawableToBitmap(IconUtils.getAppIcon(request.getFilePath()));
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


//        Log.e("TAG/ThumbnailGenerator", "createVideoThumbnail: "
//                + "\n thumbnail width = " + thumbnail.getWidth()
//                + "\n thumbnail height = " + thumbnail.getHeight()
//                + "\n iconSize = " + iconSize
//                + "\n scale = " + scale);

        return BitmapUtils.resizeAndCropCenter(thumbnail, iconSize, true);

    }

    private Bitmap createImageThumbnail(String filePath, int iconSize) {

        //BitmapFactory.Options options = new Options();

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        if (bitmap.getWidth() <= iconSize) return bitmap;

        return BitmapUtils.resizeAndCropCenter(bitmap, iconSize, true);


    }
}
