package euphoria.psycho.knife.download;

import android.content.Context;
import android.content.Intent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.log.FileLogger;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.DocumentUtils.Consumer;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProvider;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProviderImpl;
import euphoria.psycho.share.util.ContextUtils;

public class DownloadManager implements DownloadObserver {

    private final Set<DownloadObserver> mObservers = new HashSet<>();
    private final Set<TaskRecord> mTaskRecords = new HashSet<>();
    private Context mContext;
    private ExecutorService mExecutor;
    private AppCompatActivity mActivity;
    private DownloadDatabase mDatabase;
    private DownloadAdapter mAdapter;
    private boolean mIsInitialize;
    private ThumbnailProvider mThumbnailProvider;


    private DownloadManager(Context context) {
        int numThreads = 3;
        mExecutor = Executors.newFixedThreadPool(numThreads);
        mContext = context;
        mDatabase = new DownloadDatabase(context);
        startService();
    }

    public void addObserver(DownloadObserver observer) {
        synchronized (mObservers) {
            mObservers.add(observer);
        }
    }

    private void broadcastProgress(DownloadInfo downloadInfo) {
        synchronized (mObservers) {
            for (DownloadObserver observer : mObservers) {
                observer.updateProgress(downloadInfo);
            }
        }
    }

    public void cancel(DownloadInfo downloadInfo) {
        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.ask)
                .setMessage(mActivity.getString(R.string.dialog_delete_task, downloadInfo.fileName))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    delete(downloadInfo);
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();

    }

    void delete(DownloadInfo downloadInfo) {


        synchronized (mTaskRecords) {
            filter(downloadInfo, taskRecord -> taskRecord.thread.stopDownload());

            mDatabase.delete(downloadInfo);

        }

        synchronized (mObservers) {
            mObservers.remove(downloadInfo);
            for (DownloadObserver observer : mObservers) {
                observer.deleted(downloadInfo);
            }

        }


    }

    private void filter(DownloadInfo downloadInfo, Consumer<TaskRecord> c) {
        mTaskRecords.stream()
                .filter(t -> downloadInfo._id == t.id)
                .findFirst()
                .ifPresent(c::accept);
    }

    public void fullUpdate() {
        synchronized (mTaskRecords) {
            if (mTaskRecords.isEmpty()) return;
            for (TaskRecord taskRecord : mTaskRecords) {


                if (mAdapter != null)
                    mAdapter.fullUpdate(taskRecord.info);
            }
        }
    }

    public DownloadDatabase getDatabase() {
        return mDatabase;
    }

    public boolean isInitialize() {
        return mIsInitialize;
    }

    public void setInitialize(boolean initialize) {

        mIsInitialize = initialize;
    }

    private void onFinished(DownloadInfo downloadInfo) {

        synchronized (mTaskRecords) {
            mDatabase.update(downloadInfo);
            filter(downloadInfo, t -> t.thread.stopDownload());
        }


    }

    void openContent(DownloadInfo downloadInfo) {
        if (mActivity == null) return;
        DocumentUtils.openContent(mActivity, downloadInfo.filePath, 1);
    }

    void pause(DownloadInfo downloadInfo) {

        synchronized (mTaskRecords) {
            filter(downloadInfo, t -> {
                t.thread.stopDownload();
                mTaskRecords.remove(t);
            });
        }


    }

    ThumbnailProvider provideThumbnailProvider() {
        if (mThumbnailProvider == null) {
            mThumbnailProvider = new ThumbnailProviderImpl();
        }
        return mThumbnailProvider;
    }

    public void removeObserver(DownloadObserver observer) {
        synchronized (mObservers) {
            mObservers.remove(observer);
        }
    }

    void resume(DownloadInfo downloadInfo) {

        synchronized (mTaskRecords) {
            if (mTaskRecords.stream().filter(t -> t.id == downloadInfo._id).findAny().orElse(null) == null) {
                DownloadThread thread = new DownloadThread(downloadInfo, this);
                TaskRecord taskRecord = new TaskRecord();
                taskRecord.id = downloadInfo._id;
                taskRecord.thread = thread;
                taskRecord.info = downloadInfo;
                mTaskRecords.add(taskRecord);
                mExecutor.submit(thread);
                downloadInfo.status = DownloadStatus.PENDING;

                broadcastProgress(downloadInfo);
            }
        }


    }

    public void setActivity(AppCompatActivity activity) {


        mActivity = activity;
    }

    public void setAdapter(DownloadAdapter adapter) {
        mAdapter = adapter;
    }

    private void startService() {
        Intent downloadService = new Intent(mContext, DownloadService.class);

        mContext.startService(downloadService);
    }

    public static DownloadManager instance() {
        return Singleton.INSTANCE;
    }

    @Override
    public void deleted(DownloadInfo downloadInfo) {

    }

    @Override
    public void retried(DownloadInfo downloadInfo) {


    }

    @Override
    public void updateProgress(DownloadInfo downloadInfo) {


        switch (downloadInfo.status) {
            case DownloadStatus.STARTED: {
                break;
            }
            case DownloadStatus.IN_PROGRESS: {
                break;
            }
            case DownloadStatus.PAUSED: {
                onFinished(downloadInfo);
                break;
            }
            case DownloadStatus.COMPLETED: {
                onFinished(downloadInfo);
                break;
            }

            case DownloadStatus.FAILED: {
                onFinished(downloadInfo);

                break;
            }
            case DownloadStatus.RETIRED: {
                break;
            }
            case DownloadStatus.PENDING: {
                break;
            }
        }
        broadcastProgress(downloadInfo);

    }


    @Override
    public void updateStatus(DownloadInfo downloadInfo) {


        mDatabase.update(downloadInfo);

    }

    private static class Singleton {
        private static final DownloadManager INSTANCE =
                new DownloadManager(ContextUtils.getApplicationContext());
    }

    private static class TaskRecord {
        private long id;
        private DownloadThread thread;
        private DownloadInfo info;
    }
}
