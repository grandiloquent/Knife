package euphoria.psycho.share.util;

public class KeyUtils {

    public static String getCrc64(String text) {

        return Long.toString(Utils.crc64Long(text));
    }
}
