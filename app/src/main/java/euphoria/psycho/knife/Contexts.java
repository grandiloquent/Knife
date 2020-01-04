package euphoria.psycho.knife;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import euphoria.common.Strings;

public class Contexts {

    private static Context sContext;


    public static int getColor(Context context, int id) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.getColor(id);
        } else {
            return context.getResources().getColor(id);
        }
    }
    public static Context getContext() {
        return sContext;
    }
    public static void setContext(Context context) {
        sContext = context;
    }
    public static String getFilePath(String uri) {
        if (uri.startsWith("content://")
                && uri.contains("/external_files/")) {
            return Environment.getExternalStorageDirectory().getAbsolutePath() + '/' + Strings.substringAfter(uri, "/external_files/");
        } else if (uri.startsWith("file://")) {
            return Uri.decode(uri.substring("file://".length()));
        }
        return uri;
    }
    public static int getStatusBarHeight(Activity activity) {
        final Rect rect = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }
    public static CharSequence getText(ClipboardManager clipboardManager) {
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null) return null;
        if (clipData.getItemCount() > 0) {
            return clipData.getItemAt(0).getText();
        }
        return null;
    }
    public static CharSequence getText() {
        if (sContext != null) {
            ClipboardManager manager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                manager = sContext.getSystemService(ClipboardManager.class);
            } else {
                manager = (ClipboardManager) sContext.getSystemService(Context.CLIPBOARD_SERVICE);
            }
            if (manager == null) return null;
            ClipData clipData = manager.getPrimaryClip();
            if (clipData == null) return null;
            if (clipData.getItemCount() > 0) {
                return clipData.getItemAt(0).getText();
            }
        }
        return null;
    }
    public static void setText(String text) {
        if (sContext != null) {
            ClipboardManager manager = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                manager = sContext.getSystemService(ClipboardManager.class);
            } else {
                manager = (ClipboardManager) sContext.getSystemService(Context.CLIPBOARD_SERVICE);
            }
            if (manager != null)
                setText(manager, text);
        }
    }
    public static WindowManager getWindowManager(Context context) {
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            return context.getSystemService(WindowManager.class);
        }
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }
    public static void setText(Context context, String text) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) manager.setPrimaryClip(ClipData.newPlainText(null, text));
    }
    public static void setText(ClipboardManager manager, String text) {
        manager.setPrimaryClip(ClipData.newPlainText(null, text));
    }
    public static void setText(ClipboardManager manager, CharSequence text) {
        manager.setPrimaryClip(ClipData.newPlainText(null, text));
    }
    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

}
