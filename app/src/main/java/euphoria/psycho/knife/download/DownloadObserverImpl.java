package euphoria.psycho.knife.download;


import android.os.Handler;

import euphoria.psycho.common.Log;
import euphoria.psycho.common.ThreadUtils;

public class DownloadObserverImpl implements DownloadObserver {
    private final DownloadAdapter mAdapter;

    public DownloadObserverImpl(DownloadAdapter adapter) {
        mAdapter = adapter;

    }

    @Override
    public void completed(DownloadInfo downloadInfo) {


    }

    @Override
    public void updateStatus(DownloadInfo downloadInfo) {


    }

    @Override
    public void updateProgress(DownloadInfo downloadInfo) {

        ThreadUtils.postOnUiThread(() -> {
            mAdapter.updateItem(downloadInfo);
        });
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
}
