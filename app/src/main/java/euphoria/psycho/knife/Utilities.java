package euphoria.psycho.knife;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

public class Utilities {


    public static boolean isNullOrWhiteSpace(String value) {
        if (value == null) return true;

        for (int i = 0; i < value.length(); i++) {
            if (!Character.isWhitespace(value.charAt(i))) return false;
        }

        return true;
    }

    public static void setClipboardText(Context context, String text) {
        if (context == null || TextUtils.isEmpty(text)) return;

        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;
        ClipData clipData = ClipData.newPlainText(null, text);
        manager.setPrimaryClip(clipData);
    }
}

