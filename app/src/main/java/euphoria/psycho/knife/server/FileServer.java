package euphoria.psycho.knife.server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import androidx.annotation.Nullable;
import euphoria.psycho.knife.R;
import euphoria.psycho.share.util.HttpUtils;
import euphoria.psycho.share.util.NotificationUtils;
import euphoria.psycho.share.util.StorageUtils;
import euphoria.psycho.share.util.SystemUtils;

public class FileServer extends Service {
    public static final String DEFAULT_CHANNEL_NAME = "File Server";
    public static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_CHANNEL_ID = "server";
    private static final String DEFAULT_STATIC_DIRECTORY = "static";
    private static final String DEFAULT_UPLOAD_DIRECTORY = "upload";
    private NotificationManager mManager;
    private static final int FOREGROUND_ID = 1;
    WebServer mWebServer;

    public String getServerURL() {
        return mWebServer.getURL();
    }

    private void startServer() {
        if (mWebServer != null) return;
        mWebServer = new WebServer(SystemUtils.getDeviceIP(this), DEFAULT_PORT);
        File staticDirectory = new File(Environment.getExternalStorageDirectory(), DEFAULT_STATIC_DIRECTORY);
        File uploadDirectory = new File(StorageUtils.getSDCardPath(), DEFAULT_UPLOAD_DIRECTORY);

        unpackAssets();
        mWebServer.setStaticDirectory(staticDirectory);
        mWebServer.setUploadDirectory(uploadDirectory);
        mWebServer.setStartDirectory(Environment.getExternalStorageDirectory());


    }

    private void unpackAssets() {

        AssetManager assetManager = getAssets();
        try {
            String[] files = assetManager.list("/static/");
            for (String file : files) {

                Log.e("TAG/FileServer", "unpackAssets: " + file);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationUtils.createNotificationChannel(mManager, DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);

        Notification notification = NotificationUtils.createNotification(this, DEFAULT_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_phonelink_blue_24px)
                .build();
        startForeground(FOREGROUND_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startServer();
        return START_NOT_STICKY;
    }
}
