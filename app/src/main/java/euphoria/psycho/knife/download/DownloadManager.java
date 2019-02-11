package euphoria.psycho.knife.download;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadManager implements DownloadObserver {

    ExecutorService mExecutor;

    public DownloadManager() {
        int numThreads = 3;
        mExecutor = Executors.newFixedThreadPool(numThreads);
    }

    @Override
    public void completed(DownloadInfo downloadInfo) {

    }

    @Override
    public void updateStatus(DownloadInfo downloadInfo) {

    }

    @Override
    public void updateProgress(DownloadInfo downloadInfo) {

    }

    @Override
    public void paused(DownloadInfo downloadInfo) {

    }

    @Override
    public void failed(DownloadInfo downloadInfo) {

    }

    @Override
    public void retried(DownloadInfo downloadInfo) {

    }

    @Override
    public void deleted(DownloadInfo downloadInfo) {

    }


    public void resume(DownloadInfo downloadInfo) {

    }

    public void pause(DownloadInfo downloadInfo) {

    }

    public void cancel(DownloadInfo downloadInfo) {

    }
}
