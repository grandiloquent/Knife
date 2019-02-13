package euphoria.psycho.knife.download;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.Nullable;
import euphoria.psycho.common.Log;
import euphoria.psycho.knife.service.Job;

import static euphoria.psycho.common.C.DEBUG;


public class DownloadService extends Service implements DownloadObserver {
    public static final String ACTION_DOWNLOAD_CANCEL =
            "euphoria.psycho.knife.download.DOWNLOAD_CANCEL";
    public static final String ACTION_DOWNLOAD_PAUSE =
            "euphoria.psycho.knife.download.DOWNLOAD_PAUSE";
    public static final String EXTRA_CANCEL = "cancel";
    static final String EXTRA_DOWNLOAD_ID =
            "euphoria.psycho.knife.download.DownloadContentId_Id";
    static final String EXTRA_NOTIFICATION_BUNDLE_ICON_ID = "Chrome.NotificationBundleIconIdExtra";
    private static final int NOTIFICATION_ID_PROGRESS = 0;
    private static final String TAG = "TAG/" + DownloadService.class.getSimpleName();
    private final Map<Long, DownloadInfo> mDownloadInfos = new HashMap<>();
    private final AtomicReference<DownloadInfo> mForegroundDownloadInfo = new AtomicReference<>();
    protected NotificationManager mNotificationManager;
    protected ForegroundManager mForegroundManager;
    protected int mLastServiceId;
    private PowerManager mPowerManager;
    private WakeLock mWakeLock;

    protected void handleCancel(Intent intent) {

    }

    protected void handleIntent(Intent intent) {
//        if (mWakeLock == null) {
//            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
//        }
//        mWakeLock.acquire();
        String action = intent.getAction();
        if (action == null) return;
        ;
        if (action.equals(ACTION_DOWNLOAD_PAUSE)){
            long id = intent.getLongExtra(EXTRA_DOWNLOAD_ID, 0);
            if (id < 1) {

                Log.e("TAG/DownloadService", "handleIntent: invalid id = " + id);

                return;
            }
            DownloadInfo downloadInfo = mDownloadInfos.get(id);
            if (downloadInfo == null) {

                Log.e("TAG/DownloadService", "handleIntent: 在表中无对应对象 id = " + id);
                return;
            }
            DownloadManager.instance().pause(downloadInfo);
        }
    }

    private String provideChannelId() {

        return "default";
    }

    protected String provideChannelName() {
        return "Download";
    }

    protected int provideImportance() {
        return NotificationManager.IMPORTANCE_LOW;
    }

    protected void setUpNotificationChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    provideChannelId(),
                    provideChannelName(),
                    provideImportance()
            );
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    private void updateForgroundState(DownloadInfo downloadInfo) {
        DownloadInfo candidate = mDownloadInfos.isEmpty() ? null : mDownloadInfos.values().iterator().next();
        if (mForegroundDownloadInfo.compareAndSet(downloadInfo, candidate)) {
            if (candidate == null) {
                mForegroundManager.stopForeground(true);
            } else {
                Notification notification = DownloadUtils.buildNotification(this, provideChannelId(), candidate);
                mForegroundManager.startForeground(NOTIFICATION_ID_PROGRESS, notification
                );
                mNotificationManager.notify(String.valueOf(candidate._id), NOTIFICATION_ID_PROGRESS, notification);
            }
        }
    }

    private static ForegroundManager createForegroundManager(final Service service) {
        return new ForegroundManager() {
            @Override
            public void startForeground(int id, Notification notification) {
                service.startForeground(id, notification);
            }

            @Override
            public void stopForeground(boolean removeNotification) {
                service.stopForeground(removeNotification);
            }
        };
    }

    @Override
    public void completed(DownloadInfo downloadInfo) {

    }

    @Override
    public void deleted(DownloadInfo downloadInfo) {

    }

    @Override
    public void failed(DownloadInfo downloadInfo) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mForegroundManager == null) {
            mForegroundManager = createForegroundManager(this);
        }
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        setUpNotificationChannel();
        if (mPowerManager == null) {
            mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        }


        DownloadManager.instance().addObserver(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        Log.e("TAG/DownloadService", "onStartCommand: "
                + "\n  intent action = " + intent.getAction());

        if (intent.hasExtra(EXTRA_CANCEL)) {
            handleCancel(intent);
        } else {

            handleIntent(intent);
        }
        mLastServiceId = startId;

        return START_NOT_STICKY;
    }

    @Override
    public void paused(DownloadInfo downloadInfo) {

    }

    @Override
    public void retried(DownloadInfo downloadInfo) {

    }

    @Override
    public void started(DownloadInfo downloadInfo) {

        Log.e("TAG/DownloadService", "started: ");

        synchronized (mDownloadInfos) {
            if (!mDownloadInfos.containsKey(downloadInfo._id)) {
                mDownloadInfos.put(downloadInfo._id, downloadInfo);
            }
        }
        Notification notification = DownloadUtils.buildNotification(this, provideChannelId(), downloadInfo);

        if (mForegroundDownloadInfo.compareAndSet(null, downloadInfo)) {

            mForegroundManager.startForeground(NOTIFICATION_ID_PROGRESS, notification);
        }

        // Show start up notification
        mNotificationManager.notify(
                String.valueOf(downloadInfo._id), NOTIFICATION_ID_PROGRESS, notification);
    }

    @Override
    public void updateProgress(DownloadInfo downloadInfo) {
        Notification notification = DownloadUtils.buildNotification(this, provideChannelId(), downloadInfo);

        mNotificationManager.notify(String.valueOf(downloadInfo._id), NOTIFICATION_ID_PROGRESS, notification);

    }

    @Override
    public void updateStatus(DownloadInfo downloadInfo) {

    }

    interface ForegroundManager {
        void startForeground(int id, Notification notification);

        void stopForeground(boolean removeNotification);
    }
}
