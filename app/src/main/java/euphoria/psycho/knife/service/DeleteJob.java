package euphoria.psycho.knife.service;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;
import android.os.SystemClock;

import java.io.File;
import java.util.List;

import euphoria.psycho.common.FileUtils;
import euphoria.psycho.knife.R;

public class DeleteJob extends Job {

    final List<String> mSource;
    final String mTreeUri;
    private long mDeletedContentLength = 0L;
    private volatile int mDocsProcessed = 0;

    public DeleteJob(Context context, String id, Listener listener, List<String> source) {
        super(context, id, listener, FileOperationService.OPERATION_DELETE);
        mSource = source;
        mTreeUri = FileUtils.getTreeUri().toString();

    }

    void delete(File path) {

        if (path.isDirectory()) {
            File[] files = path.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    delete(f);
                }
            }
        }
        if (path.isFile()) {

            mDeletedContentLength += path.length();
            SystemClock.sleep(1000);
        } else {

        }
        mDocsProcessed++;
        delete(path);
        if (isCanceled()) {
            return;
        }
    }


    @Override
    Builder createProgressBuilder() {
        return super.createProgressBuilder(
                mContext.getString(R.string.delete_notification_title),
                R.drawable.ic_menu_delete,
                mContext.getString(android.R.string.cancel),
                R.drawable.ic_cab_cancel);
    }

    @Override
    void finish() {

    }

    @Override
    Notification getFailureNotification() {
        return getFailureNotification(
                R.plurals.delete_error_notification_title, R.drawable.ic_menu_delete);

    }

    @Override
    Notification getProgressNotification() {
        mProgressBuilder.setProgress(mSource.size(), mDocsProcessed, false);
        String format = mContext.getString(R.string.delete_progress);
        mProgressBuilder.setSubText(
                String.format(format, mDocsProcessed, mSource.size()));

        mProgressBuilder.setContentText(null);

        return mProgressBuilder.build();
    }

    @Override
    Notification getSetupNotification() {
        return getSetupNotification(mContext.getString(R.string.delete_preparing));
    }

    @Override
    Notification getWarningNotification() {
        return null;
    }

    @Override
    void start() {

        for (String path : mSource) {
            delete(new File(path));
        }
    }
}
