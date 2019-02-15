package euphoria.psycho.common;

import android.webkit.MimeTypeMap;

import java.net.URL;

import euphoria.psycho.share.util.StringUtils;

public class NetUtils {

    public static boolean isURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = StringUtils.substringAfterLast(url, ".");

        if (extension != null) {

            switch (extension) {
                case "epub":
                    type = "application/epub+zip";
                    break;

            }
        }
        if (type == null && extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        if (type == null) type = "application/*";


        return type;
    }
}
