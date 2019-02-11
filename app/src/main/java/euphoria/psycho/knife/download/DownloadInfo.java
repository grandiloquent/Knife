package euphoria.psycho.knife.download;

public class DownloadInfo {
    public long _id;
    public long bytesReceived;
    public long bytesTotal;
    public String fileName;
    public String filePath;
    public String message;
    public long speed;
    public int status;
    public String url;


    public long getRemainingMillis() {
        return speed > 0 && bytesTotal > bytesReceived ? ((bytesTotal - bytesReceived) * 1000) / speed : 0;
    }

    public int getPercent() {
        if (bytesTotal < 1) return 0;
        return (int) ((bytesReceived * 100) / bytesTotal);
    }


    public boolean isComplete() {
        return status == DownloadStatus.COMPLETED;
    }

    public boolean isPaused() {
        return status == DownloadStatus.PAUSED;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "_id=" + _id +
                ", status=" + status +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", bytesReceived=" + bytesReceived +
                ", bytesTotal=" + bytesTotal +
                ", url='" + url + '\'' +
                ", speed=" + speed +
                ", message='" + message + '\'' +
                '}';
    }

}
