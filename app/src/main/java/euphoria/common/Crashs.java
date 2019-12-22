

package euphoria.common;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Crashs implements Thread.UncaughtExceptionHandler {
    private static Crashs mInstance;
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private String mCrashTip;
    /**
     * 提交崩溃日志的URL
     */
    private String mUrl;

    private Crashs() {
    }

    protected String getCrashUrl() {
        return mUrl;
    }

    private boolean handleException(Throwable e) {
        if (null == e) {
            return false;
        }

        final StringBuilder message = new StringBuilder();
        message.append(e.getMessage()).append("\n");
        StackTraceElement[] elements = e.getStackTrace();

        if (null != elements) {
            for (StackTraceElement stack : elements) {
                message.append(stack.toString()).append('\n');
            }
        }

        new Thread() {

            @Override
            public void run() {
                //TODO write exception in file.
                Looper.prepare();
                showToast();
                appendFile(message.toString());
                Looper.loop();
            }

        }.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }

        return true;
    }

    /**
     * 初始化奔溃日志
     *
     * @param context  上下文对象
     * @param crashTip 若不为控，奔溃时，则Toast提示，若为空，则不提示
     * @param url      需要上传日志的地址
     */
    public void init(Context context, String crashTip, String url) {
        this.mContext = context;
        this.mCrashTip = crashTip;
        this.mUrl = url;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
        startExceptionService();
    }

    private void showToast() {

        Toast.makeText(mContext, mCrashTip, Toast.LENGTH_LONG).show();

    }

    private void appendFile(String message) {
        File storageDirectory = Environment.getExternalStorageDirectory();
        if (storageDirectory == null) return;
        File logFile = new File(storageDirectory, "log.txt");
        try {
            FileOutputStream out = new FileOutputStream(logFile, true);
            byte[] buf =
                    String.format("package: %s\n", mContext.getApplicationContext().getPackageName())
                            .getBytes("UTF8");
            out.write(buf);
            out.write(message.getBytes("UTF8"));
            out.write("\n\n\n".getBytes("UTF8"));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void startExceptionService() {
        if (null != mContext) {
        }
    }


    public static Crashs getInstance() {
        if (null == mInstance) {
            synchronized (Crashs.class) {
                if (mInstance == null) {
                    mInstance = new Crashs();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        if (!handleException(e) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, e);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }
}

