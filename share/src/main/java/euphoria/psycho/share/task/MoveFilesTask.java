package euphoria.psycho.share.task;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Process;
import android.provider.DocumentsContract;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.share.util.FileUtils;
import euphoria.psycho.share.util.MimeUtils;
import euphoria.psycho.share.util.StorageUtils;
import euphoria.psycho.share.util.StringUtils;

public class MoveFilesTask implements Runnable {

    private final File mDstDir;
    private final boolean mIsSDCard;
    private final Listener mListener;
    private final File[] mSrcFiles;
    private ContentResolver mContentResolver;
    private String mTreeUri;
    private List<File> mFailedFiles;
    private Uri mDstDirUri;

    public MoveFilesTask(Context context,
                         File[] srcFiles,
                         File destinationDirectory,
                         String treeUri,
                         Listener listener) {
        mIsSDCard = VERSION.SDK_INT >= VERSION_CODES.N && treeUri != null;
        if (mIsSDCard)
            mContentResolver = context.getContentResolver();
        else
            mContentResolver = null;

        mFailedFiles = new ArrayList<>();
        mSrcFiles = srcFiles;
        mDstDir = destinationDirectory;
        mTreeUri = treeUri;
        mListener = listener;
    }

    @SuppressLint("NewApi")
    private boolean sdCardToSDCard(File srcFile) {
        File targetFile = new File(mDstDir, srcFile.getName());
        if (targetFile.exists()) {
            if(!StorageUtils.deleteDocument(mContentResolver,targetFile,mTreeUri))
            return false;
        }

        try {
            return DocumentsContract.moveDocument(mContentResolver,
                    StorageUtils.getDocumentUri(srcFile, mTreeUri),
                    StorageUtils.getDocumentUri(srcFile.getParentFile(), mTreeUri),
                    mDstDirUri) != null;
        } catch (FileNotFoundException e) {

        }

        return false;
    }

    @SuppressLint("NewApi")
    private void sdCardToStorage(File srcFile, File dstDir) {
        if (mListener != null) mListener.onUpdateProgress(srcFile);
        File dstFile = new File(dstDir, srcFile.getName());
        if (srcFile.isFile()) {
            boolean result = StorageUtils.sdCardDocumentToStorageFile(mContentResolver,
                    srcFile,
                    dstFile,
                    mTreeUri,
                    true);
            if (!result) {
                mFailedFiles.add(srcFile);
            } else {
                StorageUtils.deleteDocument(mContentResolver, srcFile, mTreeUri);
            }
        } else {
            dstFile.mkdirs();
            if (dstFile.isDirectory()) {
                File[] files = srcFile.listFiles();
                if (files != null) {
                    for (File src : files) {
                        sdCardToStorage(src, dstFile);
                    }
                }
                srcFile.delete();
            } else {
                mFailedFiles.add(srcFile);
            }
        }

    }

    @SuppressLint("NewApi")
    private boolean storageToSDCard(File srcFile) {
        File targetFile = new File(mDstDir, srcFile.getName());
        if (targetFile.exists()) {
            if (!StorageUtils.deleteDocument(mContentResolver, targetFile, mTreeUri))
                return false;
        }
        InputStream is;
        OutputStream os;
        try {
            Uri targetUri = DocumentsContract.createDocument(mContentResolver, mDstDirUri,
                    MimeUtils.guessMimeTypeFromExtension(StringUtils.substringAfterLast(srcFile.getName(), ".")),
                    srcFile.getName());
            os = mContentResolver.openOutputStream(targetUri);
        } catch (FileNotFoundException e) {
            return false;
        }

        try {
            is = new FileInputStream(srcFile);
        } catch (FileNotFoundException e) {
            FileUtils.closeQuietly(os);
            return false;
        }

        try {
            FileUtils.copy(is, os);
            return true;
        } catch (IOException e) {

        } finally {
            FileUtils.closeQuietly(is);
            FileUtils.closeQuietly(os);
        }
        return false;
    }

    @SuppressLint("NewApi")
    private boolean storageToStorage(File srcFile) {
        File targetFile = new File(mDstDir, srcFile.getName());
        if (targetFile.exists()) {
            if(!targetFile.delete())
            return false;
        }
        return srcFile.renameTo(targetFile);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        if (StorageUtils.isSDCardFile(mDstDir) && mIsSDCard) {

            mDstDirUri = StorageUtils.getDocumentUri(mDstDir, mTreeUri);
            for (File srcFile : mSrcFiles) {
                if (mListener != null) mListener.onUpdateProgress(srcFile);
                if (StorageUtils.isSDCardFile(srcFile)) {
                    if (!sdCardToSDCard(srcFile)) {
                        mFailedFiles.add(srcFile);
                    }
                } else {
                    if (!storageToSDCard(srcFile)) {
                        mFailedFiles.add(srcFile);
                    } else {
                        srcFile.delete();
                    }
                }

            }
        } else {
            for (File srcFile : mSrcFiles) {
                if (mListener != null) mListener.onUpdateProgress(srcFile);

                if (mIsSDCard && StorageUtils.isSDCardFile(srcFile)) {
                    sdCardToStorage(srcFile, mDstDir);
                } else {
                    if (!storageToStorage(srcFile)) {
                        mFailedFiles.add(srcFile);
                    }
                }
            }
        }

        if (mListener != null) mListener.onFinished(mFailedFiles);
    }

    public interface Listener {

        void onFinished(List<File> failedFiles);

        void onUpdateProgress(File srcFile);
    }

}
