package euphoria.psycho.knife.helpers;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

public class Helper {
    public static void setClipboardText(Context context, String text) {
        if (context == null || TextUtils.isEmpty(text)) return;

        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) return;
        ClipData clipData = ClipData.newPlainText(null, text);
        manager.setPrimaryClip(clipData);
    }

    public static void triggerMediaScanner(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        //intent.putExtra(ACTION_SCAN_EXTERNAL, true);
        context.sendBroadcast(intent);
    }

}
