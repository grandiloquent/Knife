package euphoria.psycho.knife.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.ManagerUtils;
import euphoria.psycho.common.pool.ThreadPool;
import euphoria.psycho.knife.R;

import static euphoria.psycho.common.C.DEBUG;

public class FileOperationService extends Service implements Job.Listener {
    public static final String EXTRA_CANCEL = "cancel";
    public static final String EXTRA_JOB_ID = "job_id";
    public static final String EXTRA_OPERATION = "operation";
    public static final int OPERATION_COMPRESS = 3;
    public static final int OPERATION_COPY = 1;
    public static final int OPERATION_DELETE = 5;
    public static final int OPERATION_EXTRACT = 2;
    public static final int OPERATION_MOVE = 4;
    public static final int OPERATION_UNKNOWN = -1;
    static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID_PROGRESS = 0;
    private static final int POOL_SIZE = 2;
    private static final String TAG = "TAG/" + FileOperationService.class.getSimpleName();
    private final AtomicReference<Job> mForegroundJob = new AtomicReference<>();
    private final Map<String, JobRecord> mJobs = new HashMap<>();
    ExecutorService mDeletionExecutor;
    ExecutorService mExecutor;
    ForegroundManager mForegroundManager;
    Handler mHandler;
    private int mLastServiceId;
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    NotificationManager notificationManager;

    private ExecutorService getExecutorService(@OpType int operationType) {
        switch (operationType) {
            case OPERATION_COPY:
            case OPERATION_COMPRESS:
            case OPERATION_EXTRACT:
            case OPERATION_MOVE:
                return mExecutor;
            case OPERATION_DELETE:
                return mDeletionExecutor;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private void handleCancel(Intent intent) {
        assert (intent.hasExtra(EXTRA_CANCEL));
        assert (intent.getStringExtra(EXTRA_JOB_ID) != null);

        String jobId = intent.getStringExtra(EXTRA_JOB_ID);


        synchronized (mJobs) {
            // Do nothing if the cancelled ID doesn't match the current job ID. This prevents racey
            // cancellation requests from affecting unrelated copy jobs.  However, if the current job ID
            // is null, the service most likely crashed and was revived by the incoming cancel intent.
            // In that case, always allow the cancellation to proceed.
            JobRecord record = mJobs.get(jobId);
            if (record != null) {
                record.job.cancel();
            }
        }

        // Dismiss the progress notification here rather than in the copy loop. This preserves
        // interactivity for the user in case the copy loop is stalled.
        // Try to cancel it even if we don't have a job id...in case there is some sad
        // orphan notification.
        notificationManager.cancel(jobId, NOTIFICATION_ID_PROGRESS);

        // TODO: Guarantee the job is being finalized
    }

    private void handleOperation(String jobId, FileOperation operation) {

        synchronized (mJobs) {
            if (mWakeLock == null) {
                mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            }

            if (mJobs.containsKey(jobId)) {
                Log.e(TAG, "Duplicate job id: " + jobId
                        + ". Ignoring job request for operation: " + operation + ".");
                return;
            }

            Job job = operation.createJob(this, this, jobId);

            if (job == null) {
                return;
            }

            assert (job != null);
            if (DEBUG) Log.d(TAG, "Scheduling job " + job.getId() + ".");
            Future<?> future = getExecutorService(operation.getOpType()).submit(job);
            mJobs.put(jobId, new JobRecord(job, future));

            // Acquire wake lock to keep CPU running until we finish all jobs. Acquire wake lock
            // after we create a job and put it in mJobs to avoid potential leaking of wake lock
            // in case where job creation fails.
            mWakeLock.acquire();
        }
    }

    private void setUpNotificationChannel() {

        NotificationChannel channel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    getString(R.string.channel_name_directory),
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        // Allow tests to pre-set these with test doubles.
        if (mExecutor == null) {
            mExecutor = Executors.newFixedThreadPool(POOL_SIZE);
        }

        if (mDeletionExecutor == null) {
            mDeletionExecutor = Executors.newCachedThreadPool();
        }

        if (mHandler == null) {
            // Monitor tasks are small enough to schedule them on main thread.
            mHandler = new Handler();
        }

        if (mForegroundManager == null) {
            mForegroundManager = createForegroundManager(this);
        }

        if (notificationManager == null) {
            notificationManager = ManagerUtils.provideNotificationManager(this);
        }


        setUpNotificationChannel();

        mPowerManager = ManagerUtils.providePowerManager(this);
    }

    @Override
    public void onDestroy() {

        mHandler = null;
        mDeletionExecutor = null;
        mHandler = null;

    }

    @Override
    public void onFinished(Job job) {
        if (DEBUG) Log.d(TAG, "onStart: " + job.getId());

        Notification notification = job.getSetupNotification();
        // If there is no foreground job yet, set this job to foreground job.
        if (mForegroundJob.compareAndSet(null, job)) {
            if (DEBUG) Log.d(TAG, "Set foreground job to " + job.getId());
            mForegroundManager.startForeground(NOTIFICATION_ID_PROGRESS, notification);
        }

        // Show start up notification
        if (DEBUG) Log.d(TAG, "Posting notification for " + job.getId());
        notificationManager.notify(
                job.getId(), NOTIFICATION_ID_PROGRESS, notification);

        // Set up related monitor
        JobMonitor monitor = new JobMonitor(job, notificationManager, mHandler, mJobs);
        monitor.start();
    }

    @Override
    public void onStart(Job job) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int serviceId) {

        // TODO: Ensure we're not being called with retry or redeliver.
        // checkArgument(flags == 0);  // retry and redeliver are not supported.

        String jobId = intent.getStringExtra(EXTRA_JOB_ID);
        assert (jobId != null);


        if (intent.hasExtra(EXTRA_CANCEL)) {
            handleCancel(intent);
        } else {
            FileOperation operation = intent.getParcelableExtra(EXTRA_OPERATION);
            handleOperation(jobId, operation);
        }

        // Track the service supplied id so we can stop the service once we're out of work to do.
        mLastServiceId = serviceId;

        return START_NOT_STICKY;
    }

    @IntDef({
            OPERATION_UNKNOWN,
            OPERATION_COPY,
            OPERATION_COMPRESS,
            OPERATION_EXTRACT,
            OPERATION_MOVE,
            OPERATION_DELETE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpType {
    }

    interface ForegroundManager {
        void startForeground(int id, Notification notification);

        void stopForeground(boolean removeNotification);
    }

    private static final class JobRecord {
        private final Future<?> future;
        private final Job job;

        public JobRecord(Job job, Future<?> future) {
            this.job = job;
            this.future = future;
        }
    }

    private static final class JobMonitor implements Runnable {
        private static final long PROGRESS_INTERVAL_MILLIS = 500L;
        private final Handler mHandler;
        private final Job mJob;
        private final Object mJobsLock;
        private final NotificationManager mNotificationManager;

        private JobMonitor(Job job, NotificationManager notificationManager, Handler handler,
                           Object jobsLock) {
            mJob = job;
            mNotificationManager = notificationManager;
            mHandler = handler;
            mJobsLock = jobsLock;
        }

        private void start() {
            mHandler.post(this);
        }

        @Override
        public void run() {
            synchronized (mJobsLock) {
                if (mJob.isFinished()) {
                    // Finish notification is already shown. Progress notification is removed.
                    // Just finish itself.
                    return;
                }

                // Only job in set up state has progress bar
                if (mJob.getState() == Job.STATE_SET_UP) {
                    mNotificationManager.notify(
                            mJob.getId(), NOTIFICATION_ID_PROGRESS, mJob.getProgressNotification());
                }

                mHandler.postDelayed(this, PROGRESS_INTERVAL_MILLIS);
            }
        }
    }
}
