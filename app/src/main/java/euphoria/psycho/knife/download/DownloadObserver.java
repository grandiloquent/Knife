package euphoria.psycho.knife.download;

public interface DownloadObserver {

    void completed(DownloadInfo downloadInfo);

    void updateStatus(DownloadInfo downloadInfo);

    void updateProgress(DownloadInfo downloadInfo);

    void paused(DownloadInfo downloadInfo);

    void failed(DownloadInfo downloadInfo);

    void retried(DownloadInfo downloadInfo);

    void deleted(DownloadInfo downloadInfo);
}
