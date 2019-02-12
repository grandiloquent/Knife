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

import androidx.appcompat.app.AppCompatActivity;
import euphoria.psycho.common.ContextUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.log.FileLogger;
import euphoria.psycho.knife.App;
import euphoria.psycho.knife.DocumentUtils;
import euphoria.psycho.knife.cache.ThumbnailProvider;
import euphoria.psycho.knife.cache.ThumbnailProviderImpl;

public class DownloadManager implements DownloadObserver {

    private Context mContext;
    private ExecutorService mExecutor;
    private Map<Long, DownloadThread> mTasks = new HashMap<>();
    private AppCompatActivity mActivity;
    private List<DownloadObserver> mObservers;

    private DownloadManager(Context context) {
        int numThreads = 3;
        mExecutor = Executors.newFixedThreadPool(numThreads);
        mContext = context;
        mObservers = new ArrayList<>();
        startService();
    }

    public void addObserver(DownloadObserver observer) {
        if (mObservers.indexOf(observer) == -1)
            mObservers.add(observer);
    }

    public void cancel(DownloadInfo downloadInfo) {

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

            DownloadDatabase.instance().delete(downloadInfo);
            File downloadFile = new File(downloadInfo.filePath);
            if (downloadFile.isFile()) downloadFile.delete();
            for (DownloadObserver observer : mObservers) {
                observer.deleted(downloadInfo);
            }


        }
    }

    public void openContent(DownloadInfo downloadInfo) {
        if (mActivity == null) return;
        DocumentUtils.openContent(mActivity, downloadInfo.filePath, 1);
    }

    void pause(DownloadInfo downloadInfo) {
        synchronized (mTasks) {


            if (mTasks.containsKey(downloadInfo._id)) {
                DownloadThread thread = mTasks.get(downloadInfo._id);
                if (thread != null) {
                    FileLogger.log("TAG/DownloadManager", "pause: found the task."
                            + "_id = " + downloadInfo._id);
                    thread.stopDownload();
                    DownloadDatabase.instance().update(downloadInfo);


                }
            } else {
                FileLogger.log("TAG/DownloadManager", "pause: cant found the task."
                        + " mTasks size = " + mTasks.size()
                        + " target task _id = " + downloadInfo._id);
            }


        }
    }

    void resume(DownloadInfo downloadInfo) {

        synchronized (mTasks) {
            if (mTasks.containsKey(downloadInfo._id)) {
                FileLogger.log("TAG/DownloadManager", "resume: the task is queued. _id = " + downloadInfo._id);
                return;
            }

            DownloadThread thread = new DownloadThread(downloadInfo, mContext, this);
            mTasks.put(downloadInfo._id, thread);
            mExecutor.submit(thread);

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
    public synchronized void completed(DownloadInfo downloadInfo) {

        synchronized (mTasks) {
            DownloadDatabase.instance().update(downloadInfo);
            for (DownloadObserver observer : mObservers) {
                observer.completed(downloadInfo);
            }
        }


    }

    @Override
    public void deleted(DownloadInfo downloadInfo) {

    }

    @Override
    public void failed(DownloadInfo downloadInfo) {


    }

    @Override
    public void paused(DownloadInfo downloadInfo) {

        mTasks.remove(downloadInfo._id);
        for (DownloadObserver observer : mObservers) {
            observer.paused(downloadInfo);
        }
    }

    @Override
    public void retried(DownloadInfo downloadInfo) {


    }

    @Override
    public void updateProgress(DownloadInfo downloadInfo) {


        for (DownloadObserver observer : mObservers) {

            observer.updateProgress(downloadInfo);
        }
    }

    public ThumbnailProvider provideThumbnailProvider() {

        return new ThumbnailProviderImpl(((App)ContextUtils.getApplicationContext()).getReferencePool());
    }

    @Override
    public synchronized void updateStatus(DownloadInfo downloadInfo) {


        synchronized (mTasks) {
            DownloadDatabase.instance().update(downloadInfo);
        }

    }

    private static class Singleton {
        private static final DownloadManager INSTANCE =
                new DownloadManager(ContextUtils.getApplicationContext());
    }
}
