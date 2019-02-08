package euphoria.psycho.common;

import android.content.ClipData;
import android.content.ClipboardManager;

public class StringUtils {
    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }

    public static String getText(ClipboardManager manager) {
        ClipData clip = manager.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {

            CharSequence text = clip.getItemAt(0).getText();
            if (text != null) {
                return text.toString();
            }
        }
        return null;
    }

    public static String substringAfter(String text, String subString) {
        int i = text.indexOf(subString);
        if (i == -1) return null;
        return text.substring(i + subString.length());
    }

    public static String substringAfterLast(String text, String subString) {
        int i = text.lastIndexOf(subString);
        if (i == -1) return null;
        return text.substring(i + subString.length());
    }

    public static String substringBefore(String text, String subString) {
        int i = text.indexOf(subString);
        if (i == -1) return null;
        return text.substring(0, i);
    }

    public static String substringBeforeLast(String text, String subString) {
        int i = text.lastIndexOf(subString);
        if (i == -1) return text;
        return text.substring(0, i);
    }
}
