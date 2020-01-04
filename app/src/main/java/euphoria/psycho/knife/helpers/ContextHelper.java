package euphoria.psycho.knife.helpers;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.common.C;
import euphoria.psycho.common.widget.ListMenuButton.Item;
import euphoria.psycho.knife.DocumentInfo;
import euphoria.psycho.knife.R;

import android.app.Notification.Builder;

public class ContextHelper {
    private static final String TAG = "TAG/" + ContextHelper.class.getSimpleName();

    public static Item[] generateListMenu(Context context, DocumentInfo documentInfo) {
        List<Item> items = new ArrayList<>();

        items.add(new Item(context, R.string.rename, true));
        items.add(new Item(context, R.string.delete, true));
        items.add(new Item(context, R.string.share, true));
        items.add(new Item(context, R.string.properties, true));
        items.add(new Item(context, R.string.copy_file_name, true));
        items.add(new Item(context, R.string.add_to_archive, true));

        switch (documentInfo.getType()) {
            case C.TYPE_APK:
            case C.TYPE_OTHER:
            case C.TYPE_WORD:
            case C.TYPE_AUDIO:
            case C.TYPE_EXCEL:
            case C.TYPE_PDF: {
                break;
            }
            case C.TYPE_DIRECTORY: { // 文件夹

                items.add(new Item(context, R.string.add_bookmark, true));
                break;
            }
            case C.TYPE_TEXT: {
                items.add(new Item(context, R.string.copy_content, true));
                break;
            }
            case C.TYPE_VIDEO: {// 视频
                items.add(new Item(context, R.string.trim_video, true));
                break;
            }
            case C.TYPE_EPUB: {
                items.add(new Item(context, R.string.format_file_name, true));
                items.add(new Item(context, R.string.extract, true));

                break;
            }
            case C.TYPE_ZIP: {
                items.add(new Item(context, R.string.extract, true));


                break;
            }
        }

        return items.toArray(new Item[0]);
    }

    public static void launch(Activity activity, String fullPath, String title) {
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(
                        Uri.fromFile(new File(fullPath)),
                        FileHelper.getMimeTypeForIntent(fullPath));
        activity.startActivity(Intent.createChooser(intent, title));

    }

    public static Builder createNotification(Context context, String channelId) {

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            return new Builder(context, channelId);
        } else {
            return new Builder(context);
        }
    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }
    public static void writeToClipboard(Context context, String text) {

        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText(null, text));
    }

    public static void writeToClipboard(ClipboardManager clipboardManager, String label, String text) {

        clipboardManager.setPrimaryClip(ClipData.newPlainText(label, text));
    }
    public static String getDeviceIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            @SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();


            InetAddress inetAddress = intToInetAddress(wifiInfo.getIpAddress());

            return inetAddress.getHostAddress();
        } catch (Exception e) {


            return null;
        }
    }


    @TargetApi(VERSION_CODES.O)
    public static NotificationChannel createNotificationChannel(NotificationManager manager, String channelId, String channelName, int importance) {
        NotificationChannel channel;
        channel = new NotificationChannel(
                channelId,
                channelName,
                importance
        );

        manager.createNotificationChannel(channel);
        return channel;
    }
}
