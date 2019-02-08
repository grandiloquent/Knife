package euphoria.psycho.common;

import android.webkit.MimeTypeMap;

public class NetUtils {
    private static final String TAG = "TAG/" + NetUtils.class.getSimpleName();

    public static String getMimeType(String url) {
        String type = null;
        String extension = StringUtils.substringAfterLast(url, ".");
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null) {

            switch (extension) {
                case "epub":
                    type = "application/epub+zip";
                    break;
                default:
                    type = "application/*";
                    break;
            }
        }
        return type;
    }
}
