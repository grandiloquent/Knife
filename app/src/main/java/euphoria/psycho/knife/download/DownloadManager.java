package euphoria.psycho.knife.download;

import android.content.Context;
import android.content.Intent;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.log.FileLogger;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProvider;
import euphoria.psycho.knife.util.ThumbnailUtils.ThumbnailProviderImpl;
import euphoria.psycho.share.util.ContextUtils;

public class DownloadManager implements DownloadObserver {

    private final CopyOnWriteArrayList<DownloadObserver> mObservers = new CopyOnWriteArrayList<>();
    private Context mContext;
    private ExecutorService mExecutor;
    private AppCompatActivity mActivity;
    private DownloadDatabase mDatabase;
    private CopyOnWriteArrayList<TaskRecord> mTaskRecords = new CopyOnWriteArrayList<>();
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
        if (mObservers.indexOf(observer) == -1)
            mObservers.add(observer);
    }

    public void removeObserver(DownloadObserver observer) {
        int index = mObservers.indexOf(observer);
        if (index != -1) {
            mObservers.remove(index);
        }
    }

    private void broadcastProgress(DownloadInfo downloadInfo) {
        for (DownloadObserver observer : mObservers) {
            observer.updateProgress(downloadInfo);
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

        int index = lookupTask(downloadInfo._id);
        if (index != -1) {
            FileLogger.log("TAG/DownloadManager", "delete: " + "表中包含此任务的键");

            DownloadThread thread = mTaskRecords.get(index).thread;
            if (thread != null) {
                thread.stopDownload();
            } else {
                FileLogger.log("TAG/DownloadManager", "delete: " +
                        "表中包含此任务的键,但未找到对应的线程");
            }

        }

        mDatabase.delete(downloadInfo);
//        File downloadFile = new File(downloadInfo.filePath);
//        if (downloadFile.isFile()) downloadFile.delete();
        for (DownloadObserver observer : mObservers) {
            observer.deleted(downloadInfo);
        }


    }

    public void fullUpdate() {
        if (mTaskRecords.isEmpty()) return;
        for (TaskRecord taskRecord : mTaskRecords) {


            Log.e("TAG/DownloadManager", "fullUpdate: ");

            if (mAdapter != null)
                mAdapter.fullUpdate(taskRecord.info);
        }
    }

    public DownloadDatabase getDatabase() {
        return mDatabase;
    }

    private int lookupTask(long id) {
        int length = mTaskRecords.size();
        for (int i = 0; i < length; i++) {
            if (mTaskRecords.get(i).id == id) return i;
        }
        return -1;
    }

    private void onFinished(DownloadInfo downloadInfo) {

        mDatabase.update(downloadInfo);
        int index = lookupTask(downloadInfo._id);

        if (index != -1) {
            mTaskRecords.remove(index);
        }


    }

    void openContent(DownloadInfo downloadInfo) {
        if (mActivity == null) return;
        DocumentUtils.openContent(mActivity, downloadInfo.filePath, 1);
    }

    void pause(DownloadInfo downloadInfo) {


        int index = lookupTask(downloadInfo._id);

        if (index != -1) {
            DownloadThread thread = mTaskRecords.get(index).thread;
            if (thread != null) {
                thread.stopDownload();
            }
        } else {
            FileLogger.log("TAG/DownloadManager", "[异常] [pause]: "
                    + "\n 停止任务列表中的指定任务"
                    + "\n 目标任务的ID = " + downloadInfo._id
                    + "\n 任务列表种包含的任务数 = " + mTaskRecords.size());

        }


    }

    ThumbnailProvider provideThumbnailProvider() {
        if (mThumbnailProvider == null) {
            mThumbnailProvider = new ThumbnailProviderImpl();
        }
        return mThumbnailProvider;
    }

    void resume(DownloadInfo downloadInfo) {

        int index = lookupTask(downloadInfo._id);

        if (index != -1) {
            FileLogger.log("TAG/DownloadManager", "任务列表中已包含此任务 id = " + downloadInfo._id);
            return;
        }

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

    public void setActivity(AppCompatActivity activity) {


        mActivity = activity;
    }

    public void setAdapter(DownloadAdapter adapter) {
        mAdapter = adapter;
    }

    public boolean isInitialize() {
        return mIsInitialize;
    }

    public void setInitialize(boolean initialize) {

        mIsInitialize = initialize;
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
