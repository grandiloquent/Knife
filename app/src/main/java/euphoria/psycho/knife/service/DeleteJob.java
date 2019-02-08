package euphoria.psycho.knife.service;

import android.app.Notification;
import android.app.Notification.Builder;
import android.content.Context;
import android.os.SystemClock;

import java.io.File;
import java.util.List;

import androidx.documentfile.provider.DocumentFile;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.knife.R;

public class DeleteJob extends Job {

    final List<String> mSource;
    final String mTreeUri;
    private long mDeletedContentLength = 0L;

    public DeleteJob(Context context, String id, Listener listener, List<String> source) {
        super(context, id, listener);
        assert context != null;
        mSource = source;
        mTreeUri = StorageUtils.getTreeUri().toString();
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
    Notification getProgressNotification() {
        return null;
    }

    @Override
    Notification getSetupNotification() {
        return getSetupNotification(mContext.getString(R.string.delete_preparing));
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

            mDeletedContentLength += path.length();
            SystemClock.sleep(1000);
        } else {

        }
    }

    @Override
    void start() {

        for (String path : mSource) {
            delete(new File(path));
        }
    }
}
