package euphoria.psycho.knife.download;


public class DownloadRequestException extends Exception {
    private final int mFinalStatus;

    public DownloadRequestException(int finalStatus, String message) {
        super(message);
        mFinalStatus = finalStatus;
    }

    public DownloadRequestException(int finalStatus, Throwable t) {
        this(finalStatus, t.getMessage());
        initCause(t);
    }

    public int getFinalStatus() {
        return mFinalStatus;
    }
}