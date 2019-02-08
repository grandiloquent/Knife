package euphoria.psycho.knife;

import android.os.AsyncTask;
import android.os.Process;


public class DeleteFileTask implements Runnable {
    private final String mDirectory;


    public DeleteFileTask(String directory) {
        mDirectory = directory;

    }


    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);



    }
}
