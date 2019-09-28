package euphoria.psycho.knife.download;

import android.content.Context;

import java.io.File;

import euphoria.common.Strings;
import euphoria.psycho.knife.R;

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

    public String getDisplayName() {
        String name = Strings.substringAfter(url, "://");
        if (name == null) return "";
        String hostName = Strings.substringBefore(name, "/");
        if (hostName == null) return name;
        return hostName;
    }

    public long getFileSize() {
        File file = new File(filePath);
        if (file.isFile()) return file.length();
        return 0;
    }

    public String getStatusString(Context context) {

        switch (status) {
            case DownloadStatus.STARTED: {
                return context.getString(R.string.download_started);
            }
            case DownloadStatus.IN_PROGRESS: {
                return android.text.format.Formatter.formatFileSize(context, bytesReceived);
            }
            case DownloadStatus.PAUSED: {
                return context.getString(R.string.download_notification_paused);
            }
            case DownloadStatus.COMPLETED: {

                return context.getString(R.string.download_notification_completed);
            }
            case DownloadStatus.FAILED: {
                return context.getString(R.string.download_notification_failed);
            }
            case DownloadStatus.RETIRED: {
                return context.getString(R.string.download_notification_retry, message);
            }
            case DownloadStatus.PENDING: {
                return context.getString(R.string.download_notification_pending);
            }
        }


        return context.getString(R.string.dialog_delete_message);
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
