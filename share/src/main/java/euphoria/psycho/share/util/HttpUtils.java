package euphoria.psycho.share.util;

import java.net.URL;

public class HttpUtils {

    public static boolean isValidURL(String url) {

        try {
            new URL(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
