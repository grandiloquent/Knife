package euphoria.psycho.knife.download;


import euphoria.psycho.share.util.ThreadUtils;

public class DownloadObserverImpl implements DownloadObserver {
    private final DownloadAdapter mAdapter;

    public DownloadObserverImpl(DownloadAdapter adapter) {
        mAdapter = adapter;

    }


    @Override
    public void deleted(DownloadInfo downloadInfo) {

        mAdapter.removeItem(downloadInfo);
    }




    @Override
    public void retried(DownloadInfo downloadInfo) {


    }


    @Override
    public void updateProgress(DownloadInfo downloadInfo) {
//        switch (downloadInfo.status) {
//            case DownloadStatus.STARTED: {
//                break;
//            }
//            case DownloadStatus.IN_PROGRESS: {
//                break;
//            }
//            case DownloadStatus.PAUSED: {
//                break;
//            }
//            case DownloadStatus.COMPLETED: {
//                break;
//            }
//            case DownloadStatus.CANCELLED: {
//                break;
//            }
//            case DownloadStatus.FAILED: {
//                break;
//            }
//            case DownloadStatus.RETIRED: {
//                break;
//            }
//            case DownloadStatus.PENDING: {
//                break;
//            }
//        }

        ThreadUtils.postOnUiThread(() -> {
            mAdapter.updateItem(downloadInfo);
        });
    }



    @Override
    public void updateStatus(DownloadInfo downloadInfo) {


    }
}
