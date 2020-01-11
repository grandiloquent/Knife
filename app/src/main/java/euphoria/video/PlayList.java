package euphoria.video;

import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

import euphoria.common.Files;
import euphoria.common.Files.FileSort;

public class PlayList {

    private String mVideoDirectory;
    private int mCurrentIndex;
    private FileSort mFileSort;
    private boolean mIsAscending;
    private File[] mVideoFiles;


    public PlayList() {

    }

    public String currentVideoPath() {

        if (mVideoFiles == null || mVideoFiles.length == 0) return null;
        return mVideoFiles[mCurrentIndex].getAbsolutePath();
    }

    public void deleteCurrent() {
        String videoPath = currentVideoPath();
        if (videoPath == null) return;
        File videoFile = new File(videoPath);
        if (videoFile.isFile()) videoFile.delete();
        String nextVideoPath = nextVideoPath();
        mVideoFiles =Arrays.stream(mVideoFiles).
                filter(f -> !f.getAbsolutePath().equals(videoPath))
                .toArray(File[]::new);
        for (int i = 0; i < mVideoFiles.length; i++) {
            if (mVideoFiles[i].getAbsolutePath().equals(nextVideoPath)) {
                mCurrentIndex = i;
                return;
            }
        }
    }

    public String nextVideoPath() {
        if (mVideoFiles == null || mVideoFiles.length == 0) return null;
        if (mCurrentIndex + 1 < mVideoFiles.length) mCurrentIndex++;
        else mCurrentIndex = 0;
        return mVideoFiles[mCurrentIndex].getAbsolutePath();
    }

    public String previousVideoPath() {
        if (mVideoFiles == null || mVideoFiles.length == 0) return null;
        if (mCurrentIndex - 1 > -1) mCurrentIndex--;
        else mCurrentIndex = mVideoFiles.length - 1;
        return mVideoFiles[mCurrentIndex].getAbsolutePath();
    }

    public void updatePlayList(String videoDirectory) {
        mVideoDirectory = videoDirectory;
        mFileSort = FileSort.LastModified;
        mIsAscending = false;
        updateFiles(null);
    }

    private void updateFiles(String videoPath) {
        mVideoFiles = Files.listVideoFiles(mVideoDirectory, mFileSort, mIsAscending);

        if (mVideoFiles == null || mVideoFiles.length == 0 || videoPath == null) return;
        for (int i = 0; i < mVideoFiles.length; i++) {
            if (mVideoFiles[i].getAbsolutePath().equals(videoPath)) {
                mCurrentIndex = i;
                return;
            }
        }
    }

    public boolean updatePlayList(Intent intent) {
        String videoPath = intent.getStringExtra(PlayerFragment.EXTRA_VIDEO_PATH);


        Log.e("TAG/", "Debug: updatePlayList, \n" + videoPath);

        if (videoPath == null) return false;
        mVideoDirectory = Files.getDirectoryName(videoPath);

        int sortBy = intent.getIntExtra(PlayerFragment.EXTRA_SORT, 0);
        mFileSort = FileSort.values()[sortBy];
        mIsAscending = intent.getBooleanExtra(PlayerFragment.EXTRA_DIRECTION, false);
        updateFiles(videoPath);
        return true;
    }
}
