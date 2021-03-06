package euphoria.psycho.knife.download;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;

import java.util.concurrent.TimeUnit;

import euphoria.psycho.knife.R;

import static euphoria.psycho.knife.download.DownloadService.ACTION_DOWNLOAD_CANCEL;
import static euphoria.psycho.knife.download.DownloadService.ACTION_DOWNLOAD_PAUSE;
import static euphoria.psycho.knife.download.DownloadService.EXTRA_DOWNLOAD_ID;
import static euphoria.psycho.knife.download.DownloadService.EXTRA_NOTIFICATION_BUNDLE_ICON_ID;

public class DownloadUtils {

    static final long SECONDS_PER_MINUTE = TimeUnit.MINUTES.toSeconds(1);
    static final long SECONDS_PER_HOUR = TimeUnit.HOURS.toSeconds(1);
    static final long SECONDS_PER_DAY = TimeUnit.DAYS.toSeconds(1);
    public static final int MAX_FILE_NAME_LENGTH = 25;
    static final String ELLIPSIS = "\u2026";

    public static Intent buildActionIntent(Context context, String action, long id) {
        Intent intent = new Intent(action);
        intent.setComponent(new ComponentName(context.getPackageName(), DownloadService.class.getName()));
        intent.putExtra(EXTRA_DOWNLOAD_ID, id);

        return intent;
    }

    private static void setSubText(Builder builder, String subText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setSubText(subText);
        } else {
            builder.setContentInfo(subText);
        }
    }

    public static String formatRemainingTime(Context context, long millis) {
        long secondsLong = millis / 1000;

        int days = 0;
        int hours = 0;
        int minutes = 0;
        if (secondsLong >= SECONDS_PER_DAY) {
            days = (int) (secondsLong / SECONDS_PER_DAY);
            secondsLong -= days * SECONDS_PER_DAY;
        }
        if (secondsLong >= SECONDS_PER_HOUR) {
            hours = (int) (secondsLong / SECONDS_PER_HOUR);
            secondsLong -= hours * SECONDS_PER_HOUR;
        }
        if (secondsLong >= SECONDS_PER_MINUTE) {
            minutes = (int) (secondsLong / SECONDS_PER_MINUTE);
            secondsLong -= minutes * SECONDS_PER_MINUTE;
        }
        int seconds = (int) secondsLong;

        if (days >= 2) {
            days += (hours + 12) / 24;
            return context.getString(R.string.remaining_duration_days, days);
        } else if (days > 0) {
            return context.getString(R.string.remaining_duration_one_day);
        } else if (hours >= 2) {
            hours += (minutes + 30) / 60;
            return context.getString(R.string.remaining_duration_hours, hours);
        } else if (hours > 0) {
            return context.getString(R.string.remaining_duration_one_hour);
        } else if (minutes >= 2) {
            minutes += (seconds + 30) / 60;
            return context.getString(R.string.remaining_duration_minutes, minutes);
        } else if (minutes > 0) {
            return context.getString(R.string.remaining_duration_one_minute);
        } else if (seconds == 1) {
            return context.getString(R.string.remaining_duration_one_second);
        } else {
            return context.getString(R.string.remaining_duration_seconds, seconds);
        }
    }

    public static Notification buildNotification(Context context, String channelId, DownloadInfo downloadInfo) {
        Builder builder;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            builder = new Builder(context, channelId);
        } else {
            builder = new Builder(context);
        }
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
            builder.setLocalOnly(true);
        }
        builder.setAutoCancel(true);


        int iconId = -1;
        String contentText = "";
        switch (downloadInfo.status) {
            case DownloadStatus.STARTED:
                iconId = android.R.drawable.stat_sys_download;
                contentText = context.getResources().getString(R.string.download_started);
                builder.setOngoing(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(false);
                break;
            case DownloadStatus.IN_PROGRESS:
                iconId = android.R.drawable.stat_sys_download;
                Intent pauseIntent = buildActionIntent(context,
                        ACTION_DOWNLOAD_PAUSE,
                        downloadInfo._id);
                Intent cancelIntent = buildActionIntent(context,
                        ACTION_DOWNLOAD_CANCEL,
                        downloadInfo._id);


                builder.setOngoing(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(false)
                        .addAction(R.drawable.ic_pause_white_24dp,
                                context.getResources().getString(R.string.download_notification_pause_button),
                                buildPendingIntentProvider(context, pauseIntent, (int) downloadInfo._id))
                        .addAction(R.drawable.btn_close_white,
                                context.getResources().getString(R.string.download_notification_cancel_button),
                                buildPendingIntentProvider(context, cancelIntent, (int) downloadInfo._id));
                builder.setProgress(100, downloadInfo.getPercent(), false);
                String subText = Formatter.formatFileSize(context,downloadInfo.speed)+ "/s "+DownloadUtils.formatRemainingTime(
                        context, downloadInfo.getRemainingMillis());
                setSubText(builder, subText);
                break;
            case DownloadStatus.PAUSED:
                iconId = R.drawable.ic_pause_white_24dp;
                contentText = context.getResources().getString(R.string.download_notification_paused);
                break;


        }
        Bundle extras = new Bundle();
        extras.putInt(EXTRA_NOTIFICATION_BUNDLE_ICON_ID, iconId);
        builder.setSmallIcon(iconId);
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT_WATCH) {
            builder.addExtras(extras);
        }
        builder.setContentText(contentText);
        builder.setContentTitle(DownloadUtils.getAbbreviatedFileName(
                downloadInfo.fileName, MAX_FILE_NAME_LENGTH));
        return builder.build();
    }

    private static PendingIntent buildPendingIntentProvider(Context context, Intent intent, int id) {
        return PendingIntent.getService(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static String getAbbreviatedFileName(String fileName, int limit) {
        assert limit >= 1;  // Abbreviated file name should at least be 1 characters (a...)

        if (TextUtils.isEmpty(fileName) || fileName.length() <= limit) return fileName;

        // Find the file name extension
        int index = fileName.lastIndexOf(".");
        int extensionLength = fileName.length() - index;

        // If the extension is too long, just use truncate the string from beginning.
        if (extensionLength >= limit) {
            return fileName.substring(0, limit) + ELLIPSIS;
        }
        int remainingLength = limit - extensionLength;
        return fileName.substring(0, remainingLength) + ELLIPSIS + fileName.substring(index);
    }

}
