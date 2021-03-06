package euphoria.psycho.common.base;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public abstract class BaseService extends Service {
    protected NotificationManager mNotificationManager;
    protected ForegroundManager mForegroundManager;
    protected int mLastServiceId;
    private PowerManager mPowerManager;
    public static final String EXTRA_CANCEL = "cancel";
    private WakeLock mWakeLock;

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

    }

    private static final String TAG = "TAG/" + BaseService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(EXTRA_CANCEL)) {
            handleCancel(intent);
        } else {
            handleIntent(intent);
        }
        mLastServiceId = startId;

        return START_NOT_STICKY;
    }

    protected void setUpNotificationChannel() {
        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    provideChannelId(),
                    provideChannelId(),
                    provideImportance()
            );
            mNotificationManager.createNotificationChannel(channel);
        }
    }

    protected abstract String provideChannelId();

    protected int provideImportance() {
        return NotificationManager.IMPORTANCE_LOW;
    }

    protected abstract String provideChannelName();

    protected abstract void handleCancel(Intent intent);

    protected void handleIntent(Intent intent) {
        if (mWakeLock == null) {
            mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        }
        mWakeLock.acquire();
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

    interface ForegroundManager {
        void startForeground(int id, Notification notification);

        void stopForeground(boolean removeNotification);
    }
}
