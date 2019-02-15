package euphoria.psycho.knife;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.widget.FloatingActionButton;
import euphoria.psycho.share.task.MoveFilesTask;
import euphoria.psycho.share.task.MoveFilesTask.Listener;
import euphoria.psycho.share.util.StorageUtils;
import euphoria.psycho.share.util.ThreadUtils;

public class OperationManager {
    private FloatingActionButton mPaste;
    private FloatingActionButton mClear;
    private List<DocumentInfo> mSource;
    private DirectoryFragment mFragment;
    private Listener mListener;
    private boolean mIsCopy = false;


    private OperationManager() {

    }

    public void initialize(DirectoryFragment fragment, Listener listener) {
        Activity activity = fragment.getActivity();
        mListener = listener;
        mFragment = fragment;
        mSource = new ArrayList<>();
        mPaste = activity.findViewById(R.id.paste);
        mClear = activity.findViewById(R.id.clear);

        mPaste.setIconDrawable(activity.getResources().getDrawable(R.drawable.ic_action_content_paste));
        mClear.setIconDrawable(activity.getResources().getDrawable(R.drawable.ic_action_clear));

        mPaste.setOnClickListener(this::onPaste);

        mClear.setOnClickListener(v -> {
            mSource.clear();
            hideActionButtons();
        });
    }

    private void onPaste(View view) {
        if (mIsCopy) {
            copy();
        } else {
            cut();
        }
    }

    private void copy() {
        if (mSource.size() == 0) {
            hideActionButtons();
            return;
        }

        File targetDirectory = mFragment.getDirectory();
        Context context = mFragment.getContext();
        String treeUri = FileUtils.getTreeUri().toString();

        for (DocumentInfo documentInfo : mSource) {
            File srcFile = new File(documentInfo.getPath());
            if (srcFile.getParent().equals(targetDirectory.getAbsolutePath())) continue;
            StorageUtils.copyFile(context, srcFile, targetDirectory, treeUri);
        }
        if (mListener != null) mListener.onFinished(true);
    }

    private void cut() {
        if (mSource.size() == 0) {
            hideActionButtons();
            return;
        }

        File targetDirectory = mFragment.getDirectory();
        Context context = mFragment.getContext();

        List<File> files = new ArrayList<>();
        for (DocumentInfo documentInfo : mSource) {
            files.add(new File(documentInfo.getPath()));
        }

        MoveFilesTask moveFilesTask = new MoveFilesTask(context,
                files.toArray(new File[0]),
                targetDirectory,
                DocumentUtils.getTreeUri(),
                failedFiles -> {
                    ThreadUtils.postOnUiThread(() -> {
                        if (mListener != null) mListener.onFinished(true);
                    });
                });
        ThreadUtils.postOnBackgroundThread(moveFilesTask);
//        for (DocumentInfo documentInfo : mSource) {
//            File srcFile = new File(documentInfo.getPath());
//            if (srcFile.getParent().equals(targetDirectory.getAbsolutePath())) continue;
//            FileUtils.moveFile(context, srcFile, targetDirectory);
//        }
//        if (mListener != null) mListener.onFinished(true);
    }

    public void setSource(List<DocumentInfo> source, boolean isCopy) {
        mIsCopy = isCopy;
        mSource.clear();
        mSource.addAll(source);
        showActionButtons();
        Toast.makeText(mFragment.getContext(), "文件已存放到剪切板", Toast.LENGTH_SHORT).show();
    }


    public void hideActionButtons() {
        mPaste.setVisibility(View.INVISIBLE);
        mClear.setVisibility(View.INVISIBLE);
    }

    public void showActionButtons() {
        mPaste.setVisibility(View.VISIBLE);
        mClear.setVisibility(View.VISIBLE);
    }

    public static OperationManager instance() {
        return Singleton.INSTANCE;
    }

    private static class Singleton {
        private static final OperationManager INSTANCE =
                new OperationManager();
    }

    public interface Listener {
        void onFinished(boolean needRefreshView);
    }
}
