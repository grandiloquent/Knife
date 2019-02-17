package euphoria.psycho.share.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    public static void writeToClipboard(Context context, String text) {

        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText(null, text));
    }

    public static void writeToClipboard(ClipboardManager clipboardManager, String label, String text) {

        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
    }
}
