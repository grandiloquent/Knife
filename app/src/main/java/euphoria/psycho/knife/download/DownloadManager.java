package euphoria.psycho.knife.download;

import android.content.Context;
import android.content.Intent;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.common.ContextUtils;
import euphoria.psycho.common.log.FileLogger;
import euphoria.psycho.knife.App;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.cache.ThumbnailProvider;
import euphoria.psycho.knife.cache.ThumbnailProviderImpl;

public class DownloadManager implements DownloadObserver {

    private final List<DownloadObserver> mObservers = new ArrayList<>();
    private final Map<Long, DownloadThread> mTasks = new HashMap<>();
    private Context mContext;
    private ExecutorService mExecutor;
    private AppCompatActivity mActivity;
    private DownloadDatabase mDatabase;

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
        synchronized (mTasks) {
            if (mTasks.containsKey(downloadInfo)) {
                FileLogger.log("TAG/DownloadManager", "delete: " + "表中包含此任务的键");

                DownloadThread thread = mTasks.get(downloadInfo._id);
                if (thread != null) {
                    thread.stopDownload();
                } else {
                    FileLogger.log("TAG/DownloadManager", "delete: " +
                            "表中包含此任务的键,但未找到对应的线程");
                }

            }

            mDatabase.delete(downloadInfo);
            File downloadFile = new File(downloadInfo.filePath);
            if (downloadFile.isFile()) downloadFile.delete();
            for (DownloadObserver observer : mObservers) {
                observer.deleted(downloadInfo);
            }


        }
    }

    public DownloadDatabase getDatabase() {
        return mDatabase;
    }

    private void onFinished(DownloadInfo downloadInfo) {
        synchronized (mTasks) {
            mDatabase.update(downloadInfo);
            mTasks.remove(downloadInfo._id);
        }

    }

    void openContent(DownloadInfo downloadInfo) {
        if (mActivity == null) return;
        DocumentUtils.openContent(mActivity, downloadInfo.filePath, 1);
    }

    void pause(DownloadInfo downloadInfo) {
        synchronized (mTasks) {


            if (mTasks.containsKey(downloadInfo._id)) {
                DownloadThread thread = mTasks.get(downloadInfo._id);
                if (thread != null) {
                    thread.stopDownload();
                    mDatabase.update(downloadInfo);
                }
            } else {
                FileLogger.log("TAG/DownloadManager", "[异常] [pause]: "
                        + "\n 停止任务列表中的指定任务"
                        + "\n 目标任务的ID = " + downloadInfo._id
                        + "\n 任务列表种包含的任务数 = " + mTasks.size());

            }


        }
    }

    ThumbnailProvider provideThumbnailProvider() {

        return new ThumbnailProviderImpl(((App) ContextUtils.getApplicationContext()).getReferencePool());
    }

    void resume(DownloadInfo downloadInfo) {

        synchronized (mTasks) {
            if (mTasks.containsKey(downloadInfo._id)) {
                FileLogger.log("TAG/DownloadManager", "任务列表中已包含此任务 id = " + downloadInfo._id);
                return;
            }

            DownloadThread thread = new DownloadThread(downloadInfo, this);
            mTasks.put(downloadInfo._id, thread);
            mExecutor.submit(thread);
            downloadInfo.status = DownloadStatus.PENDING;

            broadcastProgress(downloadInfo);
        }
    }

    public void setActivity(AppCompatActivity activity) {


        mActivity = activity;
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
    public synchronized void updateStatus(DownloadInfo downloadInfo) {


        synchronized (mTasks) {
            mDatabase.update(downloadInfo);
        }

    }

    private static class Singleton {
        private static final DownloadManager INSTANCE =
                new DownloadManager(ContextUtils.getApplicationContext());
    }
}
