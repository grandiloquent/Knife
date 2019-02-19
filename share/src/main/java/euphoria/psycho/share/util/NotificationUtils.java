package euphoria.psycho.share.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public class NotificationUtils {

    public static Builder createNotification(Context context, String channelId) {

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            return new Builder(context, channelId);
        } else {
            return new Builder(context);
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
