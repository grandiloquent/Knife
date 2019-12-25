package euphoria.psycho.knife.helpers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

public class Helper {
    public static void setClipboardText(Context context, String text) {
        if (context == null || TextUtils.isEmpty(text)) return;

        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;
        ClipData clipData = ClipData.newPlainText(null, text);
        manager.setPrimaryClip(clipData);
    }
}
