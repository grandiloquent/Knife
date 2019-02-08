package euphoria.psycho.knife;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import androidx.documentfile.provider.DocumentFile;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.base.Job;

public class DeleteFileJob extends Job {
    private final Context mContext;
    private final Handler mHandler;
    private final List<String> mSource;
    final String mTreeUri;
    private AlertDialog mAlertDialog;
    private long mDeletedContentLength = 0L;
    private int mDocsProcessed = 0;
    private TextView mLine1;
    private TextView mLine2;
    private ProgressBar mProgressBar;
    private static final String TAG = "TAG/" + DeleteFileJob.class.getSimpleName();

    DeleteFileJob(Context context, Listener listener, List<String> source, Handler handler) {
        super(listener);
        mSource = source;
        mHandler = handler;
        mContext = context;
        mTreeUri = StorageUtils.getTreeUri().toString();

        launchDialog();

    }

    void delete(File path) {
        if (isCanceled()) {
            return;
        }
        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    delete(f);
                }
            }
        }

        if (path.isFile()) {

            long length = path.length();
            if (deleteFile(mContext, path, mTreeUri)) {
                mDocsProcessed++;
                mDeletedContentLength += length;

            }
        } else {
            if (path.delete()) mDocsProcessed++;
        }


        updateDialog(path.getAbsolutePath());

    }

    private void launchDialog() {

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_operation_progress, null);
        mLine1 = view.findViewById(R.id.line1);
        mLine2 = view.findViewById(R.id.line2);
        mProgressBar = view.findViewById(R.id.progressBar);

        mAlertDialog = new AlertDialog.Builder(mContext)
                .setTitle("正在删除文件...")
                .setView(view)
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> cancel())
                .show();
    }

    private void updateDialog(String path) {
        mHandler.post(() -> {

            mLine1.setText(path);
            mLine2.setText("删除 " + mDocsProcessed + " 个文件, 总共释放空间 " + FileUtils.formatFileSize(mDeletedContentLength));
        });
    }

    public static boolean deleteFile(Context context, File file, String treeUri) {

        boolean result = file.delete();
        if (!result) {
            DocumentFile documentFile = StorageUtils.getDocumentFileFromTreeUri(context, treeUri, file);
            result = documentFile.delete();
        }
        return result;
    }

    @Override
    protected void finish() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mLine1.setText("删除文件操作已完成");
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void start() {

        for (String path : mSource) {
            delete(new File(path));
        }
    }
}
