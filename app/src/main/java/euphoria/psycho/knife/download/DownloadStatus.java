package euphoria.psycho.knife.download;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({DownloadStatus.STARTED, DownloadStatus.IN_PROGRESS, DownloadStatus.PAUSED, DownloadStatus.COMPLETED,
        DownloadStatus.CANCELLED, DownloadStatus.FAILED, DownloadStatus.RETIRED})
@Retention(RetentionPolicy.SOURCE)
public @interface DownloadStatus {
    int STARTED = 0;
    int IN_PROGRESS = 1;
    int PAUSED = 2;
    int COMPLETED = 3;
    int CANCELLED = 4;
    int FAILED = 5;
    int RETIRED = 6;
}