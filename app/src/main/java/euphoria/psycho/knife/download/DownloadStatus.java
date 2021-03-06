package euphoria.psycho.knife.download;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

@IntDef({DownloadStatus.STARTED, DownloadStatus.IN_PROGRESS, DownloadStatus.PAUSED, DownloadStatus.COMPLETED,
        DownloadStatus.FAILED, DownloadStatus.RETIRED, DownloadStatus.PENDING})
@Retention(RetentionPolicy.SOURCE)
public @interface DownloadStatus {
    int STARTED = 0;
    int IN_PROGRESS = 1;
    int PAUSED = 2;
    int COMPLETED = 3;
    int FAILED = 4;
    int RETIRED = 5;
    int PENDING = 6;

}