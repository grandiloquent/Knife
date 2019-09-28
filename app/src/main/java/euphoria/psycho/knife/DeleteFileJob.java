package euphoria.psycho.knife;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import euphoria.common.Files;
import euphoria.psycho.common.base.Job;
import euphoria.psycho.knife.util.StorageUtils;
import euphoria.psycho.share.util.ThreadUtils;

public class DeleteFileJob extends Job {
    private final Context mContext;
    private final DocumentInfo[] mSource;
    private AlertDialog mAlertDialog;
    private long mDeletedContentLength = 0L;
    private int mDocsProcessed = 0;
    private TextView mLine1;
    private TextView mLine2;
    private ProgressBar mProgressBar;
    private static final String TAG = "TAG/" + DeleteFileJob.class.getSimpleName();
    private final ContentResolver mContentResolver;
    private final String mTreeUri;

    DeleteFileJob(Context context, Listener listener, DocumentInfo[] source) {
        super(listener);
        mSource = source;
        mContext = context;
        mContentResolver = context.getContentResolver();
        mTreeUri = StorageUtils.getTreeUri(context);

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
            if (StorageUtils.deleteFile(mContentResolver, path, mTreeUri)) {
                mDocsProcessed++;
                mDeletedContentLength += length;
                updateDialog(path.getAbsolutePath());
            }
        } else {
            if (StorageUtils.deleteFile(mContentResolver, path, mTreeUri)) {
                mDocsProcessed++;
                updateDialog(path.getAbsolutePath());
            }
        }


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
        ThreadUtils.postOnUiThread(() -> {

            mLine1.setText(path);
            mLine2.setText("删除 " + mDocsProcessed + " 个文件, 总共释放空间 " + Files.formatFileSize(mDeletedContentLength));
        });
    }

    @Override
    protected void finish() {
        ThreadUtils.postOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAlertDialog.dismiss();
                Toast.makeText(mContext, "删除 " + mDocsProcessed + " 个文件, 总共释放空间 " + Files.formatFileSize(mDeletedContentLength), Toast.LENGTH_LONG).show();

//                mAlertDialog.setTitle("删除文件操作已完成");
//                mAlertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
//                Button positiveButton = mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
//
//                positiveButton.setText(android.R.string.ok);
//                positiveButton.setVisibility(View.VISIBLE);
//                mAlertDialog.setCancelable(true);
//                mLine1.setVisibility(View.GONE);
//                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void start() {

        for (DocumentInfo path : mSource) {
            delete(new File(path.getPath()));
        }
    }
}
