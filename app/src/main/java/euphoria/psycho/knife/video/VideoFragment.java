package euphoria.psycho.knife.video;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import euphoria.psycho.common.C;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.ManagerUtils;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.StringUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.widget.ChromeImageButton;
import euphoria.psycho.common.widget.TimeBar;
import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.R;

public class VideoFragment extends Fragment implements TimeBar.OnScrubListener {

    private static final String DEFAULT_DIRECTORY_NAME = "Videos";
    private static final int DELAY_IN_MILLIS_UPDATE_PROGRESS = 1000;
    private static final String TAG = "TAG/" + VideoFragment.class.getSimpleName();
    private AudioManager mAudioManager;
    private ConstraintLayout mController;
    private int mCurrentPlaying;
    private View mDecorView;
    private File mDirectory;
    private TextView mDuration;
    private DecimalFormat mFormat = (DecimalFormat) NumberFormat.getInstance(Locale.US);
    ChromeImageButton mForward;
    private Handler mHandler = new Handler();
    private int mLastVolume;
    private ChromeImageButton mNext;
    private ChromeImageButton mPlay;
    private List<String> mPlayList;
    private TextView mPosition;
    private ChromeImageButton mPrevious;
    ChromeImageButton mRewind;
    private TimeBar mSeekbar;
    private StringBuilder mStringBuilder = new StringBuilder();
    private Toolbar mToolbar;
    private VideoView mVideoView;
    private Runnable mProgressUpdater = this::updateProgress;
    ChromeImageButton mVolumeDown;

    private void actionDeleteVideo() {

        new AlertDialog.Builder(getContext())
                .setTitle("询问")
                .setMessage("确定删除 " + mPlayList.get(mCurrentPlaying) + " 吗?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    dialog.dismiss();
                    StorageUtils.deleteFile(getContext(), new File(mPlayList.get(mCurrentPlaying)));
                    mPlayList = getPlayList(mDirectory);
                    mCurrentPlaying--;
                    onNext(null);
                }).setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
    }

    private void bindViews(View view) {
        mToolbar = view.findViewById(R.id.toolbar);
        mVideoView = view.findViewById(R.id.video_view);
        mController = view.findViewById(R.id.controller);
        mPrevious = view.findViewById(R.id.previous);
        mPlay = view.findViewById(R.id.play);
        mNext = view.findViewById(R.id.next);
        mPosition = view.findViewById(R.id.position);
        mSeekbar = view.findViewById(R.id.seekbar);
        mDuration = view.findViewById(R.id.duration);


        mRewind = view.findViewById(R.id.rewind);
        mForward = view.findViewById(R.id.forward);
        mVolumeDown = view.findViewById(R.id.volume_down);

    }

    private List<String> getPlayList(File dir) {
        if (dir == null) {
            dir = new File(StorageUtils.getSDCardPath(), DEFAULT_DIRECTORY_NAME);
            Log.e("TAG/", dir.getAbsolutePath());
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles(pathname -> {
                String extension = StringUtils.substringAfterLast(pathname.getName(), ".");
                return extension != null && pathname.isFile() && extension.toLowerCase().equals("mp4");
            });
            if (files == null || files.length == 0) return null;

            List<String> filePaths = new ArrayList<>();
            for (File file : files) {
                filePaths.add(file.getAbsolutePath());
            }
            return filePaths;
        }
        return null;
    }

    public void hideSystemUI() {

        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void initViews() {
        mDecorView = getActivity().getWindow().getDecorView();
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mNext.setOnClickListener(this::onNext);
        mPrevious.setOnClickListener(this::onPrevious);
        mPlay.setOnClickListener(this::onPlay);
        mVideoView.setOnPreparedListener(this::onPrepared);
        mSeekbar.addListener(this);

        mRewind.setOnClickListener(v -> {
            if (mVideoView.isPlaying()) {
                int seekTo = mVideoView.getCurrentPosition() - 1000 * 10;
                if (seekTo > 0) {
                    mVideoView.seekTo(seekTo);
                }
            }
        });
        mForward.setOnClickListener(v -> {
            if (mVideoView.isPlaying()) {
                int seekTo = mVideoView.getCurrentPosition() + 1000 * 10;
                if (seekTo < mVideoView.getDuration()) {
                    mVideoView.seekTo(seekTo);
                }
            }
        });
        mAudioManager = ManagerUtils.provideAudioManager(getContext());
        mLastVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (mLastVolume == 0) {
            mVolumeDown.setImageResource(R.drawable.ic_volume_up_white_48px);
        }

        mVolumeDown.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_ALLOW_RINGER_MODES);
                    mVolumeDown.setImageResource(R.drawable.ic_volume_up_white_48px);

                } else {
                    if (mLastVolume == 0)
                        mLastVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mLastVolume, AudioManager.FLAG_PLAY_SOUND);
                    mVolumeDown.setImageResource(R.drawable.ic_volume_off_white_48px);
                }

            }
        });
    }

    @SuppressLint("NewApi")
    void loadSubtitle() {

        String path = StringUtils.substringBeforeLast(mPlayList.get(mCurrentPlaying), ".");
        if (path == null) return;
        path = path + ".srt";
        File file = new File(path);
        if (file.isFile()) {
            FileInputStream in;
            try {
                in = new FileInputStream(file);

                mVideoView.addSubtitleSource(in,
                        MediaFormat.createSubtitleFormat(MediaFormat.MIMETYPE_TEXT_SUBRIP, Locale.US.getLanguage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void loadVideo() {
        Bundle bundle = getArguments();
        String filePath;
        if (bundle == null) {

            filePath = new File(Environment.getExternalStorageDirectory(), "Videos").listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().endsWith(".mp4")) return true;
                    return false;
                }
            })[0].getAbsolutePath();
        } else
            filePath = bundle.getString(C.EXTRA_FILE_PATH);
        mDirectory = new File(filePath).getParentFile();
        mPlayList = getPlayList(mDirectory);
        mCurrentPlaying = lookup(filePath, mPlayList);
        mVideoView.setVideoPath(filePath);

    }

    private int lookup(String path, List<String> list) {
        int length = list.size();
        for (int i = 0; i < length; i++) {
            if (path.equals(list.get(i))) return i;
        }
        return -1;
    }

    private void onNext(View view) {
        if (mCurrentPlaying + 1 < mPlayList.size()) {
            mVideoView.setVideoPath(mPlayList.get(++mCurrentPlaying));
        }
    }

    private void onPlay(View view) {
        if (mVideoView.isPlaying()) {
            mHandler.removeCallbacksAndMessages(null);
            mVideoView.pause();
            mPlay.setImageResource(R.drawable.ic_play_arrow_white_48px);

        } else {
            mVideoView.start();
            mHandler.post(mProgressUpdater);
            mPlay.setImageResource(R.drawable.ic_pause_white_48px);

        }
    }

    private void onPrepared(MediaPlayer mp) {
        mVideoView.start();
        loadSubtitle();
        mSeekbar.setDuration(mVideoView.getDuration());
        mDuration.setText(getSimpleTimestampAsString(mVideoView.getDuration()));
        mToolbar.setTitle(StringUtils.substringAfterLast(mPlayList.get(mCurrentPlaying), "/"));
        updateProgress();
    }

    private void onPrevious(View view) {
        if (mCurrentPlaying - 1 > -1) {
            mVideoView.setVideoPath(mPlayList.get(--mCurrentPlaying));
        }
    }

    private void seekTo(int progress) {
        if (mVideoView.isPlaying()) {
            mVideoView.seekTo(progress);
        }
    }

    public void showSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void updateProgress() {
        if (mVideoView.isPlaying()) {

            mPosition.setText(getSimpleTimestampAsString(mVideoView.getCurrentPosition()));
            mSeekbar.setPosition(mVideoView.getCurrentPosition());
            mHandler.postDelayed(mProgressUpdater, DELAY_IN_MILLIS_UPDATE_PROGRESS);
        }
    }

    private static String getSimpleTimestampAsString(long time) {
        final long hours = time / 3600000;
        time %= 3600000;
        final long mins = time / 60000;
        time %= 60000;
        final long sec = time / 1000;
        return String.format("%02d:%02d:%02d", hours, mins, sec);
    }

    public static void show(FragmentManager manager, String filePath) {

        FragmentTransaction transaction = manager.beginTransaction();
        VideoFragment fragment = new VideoFragment();
        if (filePath != null) {
            Bundle bundle = new Bundle();
            bundle.putString(C.EXTRA_FILE_PATH, filePath);
            fragment.setArguments(bundle);
        }
        transaction.replace(R.id.container, fragment);
        transaction.commitNowAllowingStateLoss();


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((BaseActivity) getActivity()).setOnBackPressedListener(() -> {
            DirectoryFragment.show(getFragmentManager());
            return true;
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.options_video, menu);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getContext()).inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_remove:
                actionDeleteVideo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {
    }

    @Override
    public void onScrubStart(TimeBar timeBar,long position) {
        mHandler.removeCallbacks(mProgressUpdater);

    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        mHandler.post(mProgressUpdater);
        seekTo((int) position);

        Log.e("TAG/", "onScrubStop: " + position);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        bindViews(view);
        initViews();
        loadVideo();
    }
}
