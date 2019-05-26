package euphoria.psycho.knife.util;

import android.content.ClipData;
import android.content.ClipboardManager;

public class StringUtils {
    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

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

    public static boolean isDigit(String value) {
        for (int i = 0, len = value.length(); i < len; i++) {
            if (!Character.isDigit(value.charAt(i))) return false;
        }
        return true;
    }

    public static long parseDuration(String s) {
        String[] pieces = s.split(":");
        int length = pieces.length - 1;
        long r = 0;
        for (int i = length; i > -1; i--) {
            r += (long) (Integer.parseInt(pieces[i]) * Math.pow(60, length - i));
        }
        return r * 1000;
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
