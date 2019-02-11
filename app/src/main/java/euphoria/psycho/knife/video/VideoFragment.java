package euphoria.psycho.knife.video;

import android.annotation.SuppressLint;
import android.media.AudioManager;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
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
import euphoria.psycho.common.DateUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.ManagerUtils;
import euphoria.psycho.common.StorageUtils;
import euphoria.psycho.common.StringUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.common.widget.ChromeImageButton;
import euphoria.psycho.common.widget.SystemUtils;
import euphoria.psycho.common.widget.TimeBar;
import euphoria.psycho.knife.DirectoryFragment;
import euphoria.psycho.knife.R;

public class VideoFragment extends Fragment implements TimeBar.OnScrubListener {

    private static final String DEFAULT_DIRECTORY_NAME = "Videos";
    private static final int DELAY_IN_MILLIS_UPDATE_PROGRESS = 1000;
    private static final long HIDE_CONTROLLER_DELAY_IN_MILLIS = 5000;
    private static final String TAG = "TAG/" + VideoFragment.class.getSimpleName();
    private static final int TOUCH_IGNORE = 1;
    private static final int TOUCH_NONE = 3;
    private static final int TOUCH_SEEK = 2;
    private StringBuilder mStringBuilder = new StringBuilder();

    private AudioManager mAudioManager;
    private ChromeImageButton mForward;
    private ChromeImageButton mNext;
    private ChromeImageButton mPlay;
    private ChromeImageButton mPrevious;
    private ChromeImageButton mRewind;
    private ChromeImageButton mVolumeDown;
    private ConstraintLayout mController;
    private DisplayMetrics mDisplayMetrics;
    private File mDirectory;
    private float mInitTouchX = 0f;
    private float mInitTouchY = 0f;
    private float mTouchX = -1f;
    private float mTouchY = -1f;
    private Formatter mFormatter = new Formatter(mStringBuilder);
    private Handler mHandler = new Handler();
    private int mCurrentPlaying;
    private int mLastVolume;
    private int mSortBy = C.SORT_BY_NAME;
    private int mTouchAction = TOUCH_NONE;
    private List<String> mPlayList;
    private TextView mDuration;
    private TextView mPosition;
    private TimeBar mSeekbar;
    private Toolbar mToolbar;
    private VideoView mVideoView;
    private Runnable mProgressUpdater = this::updateProgress;
    private View mDecorView;
    private Runnable mVisibilityChecker = this::hide;
    private View mRootLayout;

    private void actionDeleteVideo() {
        mHandler.removeCallbacks(null);
        new AlertDialog.Builder(getContext())
                .setTitle("询问")
                .setMessage("确定删除 " + mPlayList.get(mCurrentPlaying) + " 吗?")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    dialog.dismiss();
                    StorageUtils.deleteFile(getContext(), new File(mPlayList.get(mCurrentPlaying)));
                    mPlayList = getPlayList(mDirectory);
                    mCurrentPlaying--;
                    onNext(null);
                    updateVisual();
                }).setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
            updateVisual();
        }).show();
    }

    private void bindViews(View view) {
        mRootLayout = view.findViewById(R.id.root_layout);
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

    private void doSeekTouch(int coef, float gesturesize, boolean seek) {
        if (coef == 0) coef = 1;

        if (Math.abs(gesturesize) < 1 || !mVideoView.isPlaying()) return;
        if (mTouchAction != TOUCH_NONE && mTouchAction != TOUCH_SEEK) return;
        mTouchAction = TOUCH_SEEK;

        int length = mVideoView.getDuration();
        int time = mVideoView.getCurrentPosition();
        int jump = (int) (Math.signum(gesturesize) * (600000 * Math.pow((gesturesize / 8), 4.0) + 3000) / coef);

        if (jump > 0 && time + jump > length) jump = (length - time);
        if (jump < 0 && time + jump < 0) jump = -time;

        if (seek && length > 0) mVideoView.seekTo(time + jump);

//        if (length > 0) showInfo(String.format("%s%s (%s)%s",
//                jump >= 0 ? "+" : "",
//                mVideoManager.millisToString(jump),
//                mVideoManager.millisToString(time + jump),
//                (coef > 1) ? String.format(" x%.1g", 1.0 / coef)
//                        : ""), 50);
//        else showInfo("", 1000);
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

            Collator collator = Collator.getInstance(Locale.CHINA);
            Arrays.sort(files, (o1, o2) -> {
                int diff = 0;
                switch (mSortBy) {
                    case C.SORT_BY_NAME:
                        return collator.compare(o1.getName(), o2.getName()) * -1;
                    case C.SORT_BY_SIZE:
                        diff = (int) (o1.length() - o2.length());
                        if (diff > 0) return -1;
                        else if (diff < 0) return 1;
                        else return 0;
                    case C.SORT_BY_DATE_MODIFIED:
                        diff = (int) (o1.lastModified() - o2.lastModified());
                        if (diff > 0) return -1;
                        else if (diff < 0) return 1;
                        else return 0;
                }
                return 0;
            });
            List<String> filePaths = new ArrayList<>();
            for (File file : files) {
                filePaths.add(file.getAbsolutePath());
            }
            return filePaths;
        }
        return null;
    }

    private void hide() {

        if (mController.getVisibility() == View.VISIBLE) {
            mController.setVisibility(View.INVISIBLE);
            SystemUtils.hideSystemUI(mDecorView);
            mToolbar.setVisibility(View.INVISIBLE);
        }
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
        mDisplayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        mRootLayout.setOnTouchListener(this::onTouch);

        mVolumeDown.setOnClickListener(v -> {

            if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_ALLOW_RINGER_MODES);
                mVolumeDown.setImageResource(R.drawable.ic_volume_up_white_48px);

            } else {
                if (mLastVolume == 0)
                    mLastVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mLastVolume, AudioManager.FLAG_PLAY_SOUND);
                mVolumeDown.setImageResource(R.drawable.ic_volume_off_white_48px);
            }

        });
    }

    @SuppressLint("NewApi")
    private void loadSubtitle() {

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
        } else {
            filePath = bundle.getString(C.EXTRA_FILE_PATH);
            mSortBy = bundle.getInt(C.EXTRA_SORT_BY);
        }
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
        mDuration.setText(DateUtils.getStringForTime(mStringBuilder, mFormatter, mVideoView.getDuration()));
        mToolbar.setTitle(StringUtils.substringAfterLast(mPlayList.get(mCurrentPlaying), "/"));
        updateProgress();
        mHandler.postDelayed(mVisibilityChecker, HIDE_CONTROLLER_DELAY_IN_MILLIS);
    }

    private void onPrevious(View view) {
        if (mCurrentPlaying - 1 > -1) {
            mVideoView.setVideoPath(mPlayList.get(--mCurrentPlaying));
        }
    }

    private boolean onTouch(View v, MotionEvent event) {


        float xChanged = (mTouchX != -1f && mTouchY != -1f) ? event.getRawX() - mTouchX : 0f;
        float yChanged = (xChanged != 0f) ? event.getRawY() - mTouchY : 0f;

        // coef is the gradient's move to determine a neutral zone
        // float coef = Math.abs(yChanged / xChanged);
        float xgesturesize = xChanged / mDisplayMetrics.xdpi * 2.54f;
        float deltaY = Math.max(1f, (Math.abs(mInitTouchY - event.getRawY()) / mDisplayMetrics.xdpi + 0.5f) * 2f);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mInitTouchY = event.getRawY();
                mInitTouchX = event.getRawX();
                mTouchY = mInitTouchY;
                mTouchAction = TOUCH_NONE;
                mTouchX = event.getRawX();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mTouchAction == TOUCH_IGNORE) return false;
                doSeekTouch(Math.round(deltaY), xgesturesize, false);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mTouchAction == TOUCH_IGNORE) mTouchAction = TOUCH_NONE;


                if (xgesturesize == 0.0f) {
                    show();
                } else if (mTouchAction == TOUCH_SEEK)
                    doSeekTouch(Math.round(deltaY), xgesturesize, true);
                mTouchX = -1f;
                mTouchY = -1f;
            }
        }

        return true;
    }

    private void seekTo(int progress) {
        if (mVideoView.isPlaying()) {
            mVideoView.seekTo(progress);
        }
    }

    private void show() {

        if (mController.getVisibility() == View.INVISIBLE) {
            mHandler.post(mProgressUpdater);
            SystemUtils.showSystemUI(mDecorView);
            mController.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.VISIBLE);
            mHandler.postDelayed(mVisibilityChecker, HIDE_CONTROLLER_DELAY_IN_MILLIS);
        }


    }

    private void updateProgress() {
        if (mVideoView.isPlaying() && mController.getVisibility() == View.VISIBLE) {

            mPosition.setText(DateUtils.getStringForTime(mStringBuilder, mFormatter, mVideoView.getCurrentPosition()));
            mSeekbar.setPosition(mVideoView.getCurrentPosition());
            mHandler.postDelayed(mProgressUpdater, DELAY_IN_MILLIS_UPDATE_PROGRESS);
        }
    }

    private void updateVisual() {
        mHandler.post(mProgressUpdater);
        mHandler.postDelayed(mVisibilityChecker, HIDE_CONTROLLER_DELAY_IN_MILLIS);
    }

    public static void show(FragmentManager manager, String filePath, int sortBy) {

        FragmentTransaction transaction = manager.beginTransaction();
        VideoFragment fragment = new VideoFragment();
        if (filePath != null) {
            Bundle bundle = new Bundle();
            bundle.putString(C.EXTRA_FILE_PATH, filePath);
            bundle.putInt(C.EXTRA_SORT_BY, sortBy);
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
    public void onScrubStart(TimeBar timeBar, long position) {
        mHandler.removeCallbacks(null);

    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        mHandler.post(mProgressUpdater);
        seekTo((int) position);
        mHandler.postDelayed(mVisibilityChecker, HIDE_CONTROLLER_DELAY_IN_MILLIS);

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
