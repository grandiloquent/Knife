package euphoria.video;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.net.rtp.AudioStream;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.mp4parser.srt.SrtParser;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import euphoria.common.Files;
import euphoria.common.Files.FileSort;
import euphoria.psycho.knife.R;
import euphoria.video.TimeBar.OnScrubListener;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerFragment extends ImmersiveModeFragment implements
        SurfaceTextureListener,
        PlayerFragmentDelegate,
        OnClickListener {
    public static final String EXTRA_DIRECTION = "extra_direction";
    public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_VIDEO_PATH = "extra_video_path";
    private static final int HIDE_CONTROLLER_DELAY = 5000;
    private final Handler mHandler = new Handler();
    private final PlayList mPlayList = new PlayList();
    private final PlayerEventListener mPlayerEventListener = new PlayerEventListener(this);
    private int mScreenWidth;
    private int mScreenHeight;
    private SurfaceTexture mSurfaceTexture;
    private IjkMediaPlayer mIjkMediaPlayer;
    boolean mShowing = true;
    private View mController;
    private TextView mExoDuration;
    private View mExoNext;
    private View mExoPause;
    private View mExoPlay;
    private TextView mExoPosition;
    private View mExoPrev;
    private TimeBar mExoProgress;
    private final Runnable mProgressChecker = new Runnable() {
        @Override
        public void run() {
            long pos = setProgress();
            mHandler.postDelayed(mProgressChecker, 1000 - (pos % 1000));
        }
    };
    private final Runnable mPlayingChecker = new Runnable() {
        @Override
        public void run() {
            if (mIjkMediaPlayer != null && mIjkMediaPlayer.isPlaying()) {
                hideController();
            } else {
                mHandler.postDelayed(mPlayingChecker, 250);
            }
        }
    };
    private View mExoRew;
    private View mPlayerView;
    private TextureView mTextureView;
    private PlayerViewGestureHandler mPlayerViewGestureHandler;
    private Surface mSurface;
    private Bookmarker mBookmarker;

    private View mExoMode;

    private void actionDelete() {
        if (mIjkMediaPlayer != null && mIjkMediaPlayer.isPlaying()) {
            mIjkMediaPlayer.stop();
        }
        mPlayList.deleteCurrent();
        loadVideo();
    }

    private void adjustSize() {
        Log.e("TAG/", "[adjustSize]");
        int videoHeight = mIjkMediaPlayer.getVideoHeight();
        int videoWidth = mIjkMediaPlayer.getVideoWidth();
        TextureView textureView = mTextureView;
        float textureViewWidth = textureView.getWidth();
        float textureViewHeight = textureView.getHeight();
        float width = textureViewWidth;
        float height = textureViewHeight;
        float pivotX = textureViewWidth / 2;
        float pivotY = textureViewHeight / 2;
        float videoAspectRatio = videoWidth / (float) videoHeight;
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
        if (aspectDeformation > 0) {
            height = (int) (width / videoAspectRatio);
        } else {
            width = (int) (height * videoAspectRatio);
        }
        Matrix matrix = new Matrix();
        matrix.postScale(width / textureViewWidth,
                height / textureViewHeight,
                pivotX, pivotY);
        mTextureView.setTransform(matrix);
    }

    private void debugPlay() {
        Log.e("TAG/", "[debugPlay]");
        String videoDirectory = Files.getExternalStoragePath("videos");
        mPlayList.updatePlayList(videoDirectory);
        loadVideo();
    }

    private void findViews(View view) {
        Log.e("TAG/", "[findViews]");
        mController = view.findViewById(R.id.controller);
        mExoDuration = view.findViewById(R.id.exo_duration);
        mExoNext = view.findViewById(R.id.exo_next);
        mExoPause = view.findViewById(R.id.exo_pause);
        mExoPlay = view.findViewById(R.id.exo_play);
        mExoPosition = view.findViewById(R.id.exo_position);
        mExoPrev = view.findViewById(R.id.exo_prev);
        mExoProgress = view.findViewById(R.id.exo_progress);
        mExoRew = view.findViewById(R.id.exo_rew);
        mPlayerView = view.findViewById(R.id.player_view);
        mTextureView = view.findViewById(R.id.texture_view);
        mExoMode = view.findViewById(R.id.exo_mode);

    }


    private void hideController() {
        Log.e("TAG/", "[hideController]");
        if (mPlayerEventListener.isDragging()) {
            return;
        }
        mHandler.removeCallbacks(mPlayingChecker);
        mHandler.removeCallbacks(mProgressChecker);
        hideNavigationBar();
        mController.setVisibility(View.GONE);
        mShowing = false;
    }

    private void loadVideo() {
        setupPlayer();
        try {
            mIjkMediaPlayer.reset();
            mIjkMediaPlayer.setSurface(mSurface);
            mIjkMediaPlayer.setDataSource(mPlayList.currentVideoPath());
            mIjkMediaPlayer.prepareAsync();
        } catch (IOException e) {
        }
    }

    private void next(boolean isPrev) {
        Log.e("TAG/", "[next]");
        String videoPath;
        if (!isPrev) {
            videoPath = mPlayList.nextVideoPath();
        } else {
            videoPath = mPlayList.previousVideoPath();
        }
        if (videoPath == null) {
            return;
        }
        getSupportActionBar().setTitle(Files.getFileName(mPlayList.currentVideoPath()));
        try {
            mIjkMediaPlayer.reset();
            mIjkMediaPlayer.setSurface(mSurface);
            mIjkMediaPlayer.setDataSource(videoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIjkMediaPlayer.prepareAsync();
    }

    private void play() {
        Log.e("TAG/", "[play]");
        if (mIjkMediaPlayer != null && mIjkMediaPlayer.isPlayable()) {
            mIjkMediaPlayer.start();
            mExoPlay.setVisibility(View.GONE);
            mExoPause.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mProgressChecker);
            mHandler.post(mProgressChecker);
        }
    }

    private long setProgress() {
        // Log.e("TAG/","[setProgress]");
        if (mIjkMediaPlayer == null || mPlayerEventListener.isDragging() || !mShowing) {
            return 0;
        }
        long position = mIjkMediaPlayer.getCurrentPosition();
        mExoPosition.setText(PlayerHelper.formatDuration(position / 1000));
        mExoProgress.setPosition(position);
        return position / 1000;
    }

    private void setupPlayer() {
        Log.e("TAG/", "[setupPlayer]");
        IjkMediaPlayer ijkMediaPlayer = PlayerHelper.createPlayer();
        if (mSurface == null)
            mSurface = new Surface(mSurfaceTexture);
        ijkMediaPlayer.setOnPreparedListener(mPlayerEventListener);
        ijkMediaPlayer.setOnErrorListener(mPlayerEventListener);
        ijkMediaPlayer.setOnCompletionListener(mPlayerEventListener);
        ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mIjkMediaPlayer = ijkMediaPlayer;
    }

    private void showController() {
        Log.e("TAG/", "[showController]");
        showNavigationBar();
        mController.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mProgressChecker);
        mHandler.removeCallbacks(mPlayingChecker);
        mHandler.post(mProgressChecker);
        mHandler.postDelayed(mPlayingChecker, HIDE_CONTROLLER_DELAY);
        mShowing = true;
    }

    @Override
    public int getCurrentVideoPosition() {
        Log.e("TAG/", "[getCurrentVideoPosition]");
        return 0;
    }

    @Override
    public IjkMediaPlayer getPlayer() {
        return mIjkMediaPlayer;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.e("TAG/", "[onActivityCreated]");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.e("TAG/", "[onActivityResult]");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.e("TAG/", "[onAttach]");
        super.onAttach(context);
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        Log.e("TAG/", "[onAttachFragment]");
    }

    @Override
    public void onClick(View v) {
        Log.e("TAG/", "[onClick]");
        switch (v.getId()) {
            case R.id.exo_pause:
                pause();
                return;
            case R.id.exo_play:
                play();
                return;
            case R.id.exo_next:
                next(false);
                return;
            case R.id.exo_prev:
                next(true);
                return;
            case R.id.exo_mode:
                mode();
                return;
        }
    }

    private void mode() {
        int videoHeight = mIjkMediaPlayer.getVideoHeight();
        int videoWidth = mIjkMediaPlayer.getVideoWidth();
        TextureView textureView = mTextureView;
        float textureViewWidth = textureView.getWidth();
        float textureViewHeight = textureView.getHeight();
        float width = textureViewWidth;
        float height = textureViewHeight;
        float pivotX = textureViewWidth / 2;
        float pivotY = textureViewHeight / 2;
        float videoAspectRatio = videoWidth / (float) videoHeight;
        float viewAspectRatio = (float) width / height;
        float aspectDeformation = videoAspectRatio / viewAspectRatio - 1;
        if (aspectDeformation > 0) {
            height = (int) (width / videoAspectRatio);
        } else {
            width = (int) (height * videoAspectRatio);
        }
        Matrix matrix = new Matrix();
        matrix.postScale(videoWidth / textureViewWidth,
                videoHeight / textureViewHeight,
                pivotX, pivotY);
        mTextureView.setTransform(matrix);
    }

    @Override
    public void onCompletion() {
        mHandler.removeCallbacks(mPlayingChecker);
        mHandler.removeCallbacks(mProgressChecker);
        next(false);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.e("TAG/", "[onConfigurationChanged]");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        Log.e("TAG/", "[onContextItemSelected]");
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("TAG/", "[onCreate]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v,
                                    @Nullable ContextMenuInfo menuInfo) {
        Log.e("TAG/", "[onCreateContextMenu]");

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.e("TAG/", "[onCreateOptionsMenu]");
        inflater.inflate(R.menu.video, menu);
    }

    /*Starting at here*/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e("TAG/", "[onCreateView]");


        View view = inflater.inflate(R.layout.fragment_player, container, false);
        findViews(view);
        mExoRew.setVisibility(View.GONE);
        mPlayerViewGestureHandler = new PlayerViewGestureHandler();
        mPlayerViewGestureHandler.initView(mPlayerView);
        mPlayerView.setOnTouchListener(mPlayerViewGestureHandler);
        mTextureView.setSurfaceTextureListener(this);
        mExoNext.setOnClickListener(this);
        mExoPause.setOnClickListener(this);
        mExoPlay.setOnClickListener(this);
        mExoPrev.setOnClickListener(this);
        mExoMode.setOnClickListener(this);

        mExoProgress.addListener(mPlayerEventListener);
        mBookmarker = new Bookmarker(getContext());

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onDestroy() {
        Log.e("TAG/", "[onDestroy]");
        super.onDestroy();
    }

    @Override
    public void onDestroyOptionsMenu() {
        Log.e("TAG/", "[onDestroyOptionsMenu]");
    }

    @Override
    public void onDestroyView() {
        Log.e("TAG/", "[onDestroyView]");
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        Log.e("TAG/", "[onDetach]");
        super.onDetach();
    }

    @Override
    public void onError(String message) {
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.e("TAG/", "[onHiddenChanged]");
    }

    @Override
    public void onLowMemory() {
        Log.e("TAG/", "[onLowMemory]");
        super.onLowMemory();
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        Log.e("TAG/", "[onMultiWindowModeChanged]");
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                actionDelete();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        Log.e("TAG/", "[onPause]");
        super.onPause();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        Log.e("TAG/", "[onPictureInPictureModeChanged]");
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        Log.e("TAG/", "[onPrepareOptionsMenu]");
    }

    @Override
    public void onPrepared() {
        mHandler.removeCallbacks(mProgressChecker);
        mHandler.removeCallbacks(mPlayingChecker);

        adjustSize();
        mIjkMediaPlayer.start();
        mExoProgress.setDuration(mIjkMediaPlayer.getDuration());
        mExoPosition.setText(PlayerHelper.formatDuration(0));
        mExoDuration.setText(PlayerHelper.formatDuration(mIjkMediaPlayer.getDuration() / 1000));
        mHandler.post(mProgressChecker);
        mHandler.postDelayed(mPlayingChecker, HIDE_CONTROLLER_DELAY);
        mExoPlay.setVisibility(View.GONE);
        mExoPause.setVisibility(View.VISIBLE);

        Long position = mBookmarker.getBookmark(mPlayList.currentVideoPath());
        if (position != null) {
            mIjkMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void onPrimaryNavigationFragmentChanged(boolean isPrimaryNavigationFragment) {
        Log.e("TAG/", "[onPrimaryNavigationFragmentChanged]");
    }

    @Override
    public void onResume() {
        Log.e("TAG/", "[onResume]");
        super.onResume();
        if (mSurfaceTexture != null) {
            loadVideo();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.e("TAG/", "[onSaveInstanceState]");
    }

    @Override
    public void onStart() {
        Log.e("TAG/", "[onStart]");
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        videoPlaybackStopped();
    }

    /*Starting play video in here*/
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.e("TAG/", "[onSurfaceTextureAvailable]");
        if (mScreenWidth == 0) {
            mScreenHeight = height;
            mScreenWidth = width;
        }
        mSurfaceTexture = surface;
        boolean result = mPlayList.updatePlayList(getActivity().getIntent());
        if (!result) {
            debugPlay();
        } else {
            loadVideo();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.e("TAG/", "[onSurfaceTextureDestroyed]");
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.e("TAG/", "[onSurfaceTextureSizeChanged]");
        adjustSize();
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Log.e("TAG/","[onSurfaceTextureUpdated]");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.e("TAG/", "[onViewCreated]");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.e("TAG/", "[onViewStateRestored]");
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void pause() {
        Log.e("TAG/", "[pause]");
        if (mIjkMediaPlayer == null) return;
        if (mIjkMediaPlayer.isPlaying()) {
            mIjkMediaPlayer.pause();
            mExoPause.setVisibility(View.GONE);
            mExoPlay.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mProgressChecker);
        } else {
            play();
        }
    }

    @Override
    public void videoPlaybackStopped() {
        Log.e("TAG/", "[videoPlaybackStopped]");
        if (mIjkMediaPlayer != null) {
            if (mIjkMediaPlayer.isPlaying())
                mIjkMediaPlayer.stop();
            mBookmarker.setBookmark(mPlayList.currentVideoPath(), mIjkMediaPlayer.getCurrentPosition());
            mIjkMediaPlayer.release();
            mIjkMediaPlayer = null;
        }
    }

    interface PlayerDelegate {
    }

    private static class VideoBrightness {
        private static final String BRIGHTNESS_LEVEL_PREF = "brightness";
        private float mBrightness;
        private float mInitialBrightness;
        private Activity mActivity;

        VideoBrightness(final Activity activity) {
            mActivity = activity;
            loadBrightnessFromPreference();
            mInitialBrightness = mBrightness;
            setVideoBrightness(0);
        }

        private void adjustVideoBrightness() {
            WindowManager.LayoutParams lp = mActivity.getWindow().getAttributes();
            lp.screenBrightness = mBrightness;
            mActivity.getWindow().setAttributes(lp);
        }

        String getBrightnessString() {
            return ((int) (mBrightness * 100)) + "%";
        }

        private void loadBrightnessFromPreference() {
            final float brightnessPref = PreferenceManager.getDefaultSharedPreferences(mActivity)
                    .getFloat(BRIGHTNESS_LEVEL_PREF, 1f);
            setBrightness(brightnessPref);
        }

        void onGestureDone() {
            mInitialBrightness = mBrightness;
        }

        private void saveBrightnessToPreference() {
            Context context;
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                    mActivity).edit();
            editor.putFloat(BRIGHTNESS_LEVEL_PREF, mBrightness);
            editor.apply();
        }

        private void setBrightness(float brightness) {
            if (brightness < 0) {
                brightness = 0;
            } else if (brightness > 1) {
                brightness = 1;
            }
            this.mBrightness = brightness;
        }

        void setVideoBrightness(double adjustPercent) {
            if (adjustPercent < -1.0f) {
                adjustPercent = -1.0f;
            } else if (adjustPercent > 1.0f) {
                adjustPercent = 1.0f;
            }
            setBrightness(mInitialBrightness + (float) adjustPercent);
            adjustVideoBrightness();
            saveBrightnessToPreference();
        }
    }

    private class PlayerViewGestureHandler extends PlayerViewGestureDetector {
        private static final int MAX_VIDEO_STEP_TIME = 60 * 1000;
        VideoBrightness mVideoBrightness;
        RelativeLayout mIndicatorView;
        ImageView mIndicatorImageView;
        TextView mIndicatorTextView;
        boolean mIsControllerVisible = true;
        float mStartVolumePercent = -1.0f;
        long mStartVideoTime = -1;

        PlayerViewGestureHandler() {
            super(getContext());
            mVideoBrightness = new VideoBrightness(getActivity());
        }

        private void hideIndicator() {
            mIndicatorView.setVisibility(View.GONE);
        }

        void initView(View view) {
            mIndicatorView = view.findViewById(R.id.indicatorView);
            mIndicatorImageView = view.findViewById(R.id.indicatorImageView);
            mIndicatorTextView = view.findViewById(R.id.indicatorTextView);
            // mPlayerView.setControllerVisibilityListener(visibility -> mIsControllerVisible = (visibility == View.VISIBLE));
        }

        private void showIndicator() {
            mIndicatorView.setVisibility(View.VISIBLE);
        }

        private boolean showOrHideHud() {
            if (mShowing) {
                hideController();
            } else {
                showController();
                mHandler.removeCallbacks(mPlayingChecker);
            }
            return false;
        }

        @Override
        public void adjustBrightness(double adjustPercent) {
            mVideoBrightness.setVideoBrightness(adjustPercent);
            mIndicatorImageView.setImageResource(R.drawable.ic_brightness);
            mIndicatorTextView.setText(mVideoBrightness.getBrightnessString());
            showIndicator();
        }

        @Override
        public void adjustVideoPosition(double adjustPercent, boolean forwardDirection) {
            long totalTime = mIjkMediaPlayer.getDuration();
            if (adjustPercent < -1.0f) {
                adjustPercent = -1.0f;
            } else if (adjustPercent > 1.0f) {
                adjustPercent = 1.0f;
            }
            if (mStartVideoTime < 0) {
                mStartVideoTime = mIjkMediaPlayer.getCurrentPosition();
            }
            double positiveAdjustPercent = Math.max(adjustPercent, -adjustPercent);
            long targetTime = mStartVideoTime + (long) (MAX_VIDEO_STEP_TIME * adjustPercent * (positiveAdjustPercent / 0.1));
            if (targetTime > totalTime) {
                targetTime = totalTime;
            }
            if (targetTime < 0) {
                targetTime = 0;
            }
            String targetTimeString = PlayerHelper.formatDuration(targetTime / 1000);
            if (forwardDirection) {
                mIndicatorImageView.setImageResource(R.drawable.ic_forward);
                mIndicatorTextView.setText(targetTimeString);
            } else {
                mIndicatorImageView.setImageResource(R.drawable.ic_rewind);
                mIndicatorTextView.setText(targetTimeString);
            }
            showIndicator();
            mIjkMediaPlayer.seekTo(targetTime);
        }

        @Override
        public void adjustVolumeLevel(double adjustPercent) {
            if (adjustPercent < -1.0f) {
                adjustPercent = -1.0f;
            } else if (adjustPercent > 1.0f) {
                adjustPercent = 1.0f;
            }
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            final int STREAM = AudioManager.STREAM_MUSIC;
            int maxVolume = audioManager.getStreamMaxVolume(STREAM);
            if (maxVolume == 0) return;
            if (mStartVolumePercent < 0) {
                int curVolume = audioManager.getStreamVolume(STREAM);
                mStartVolumePercent = curVolume * 1.0f / maxVolume;
            }
            double targetPercent = mStartVolumePercent + adjustPercent;
            if (targetPercent > 1.0f) {
                targetPercent = 1.0f;
            } else if (targetPercent < 0) {
                targetPercent = 0;
            }
            int index = (int) (maxVolume * targetPercent);
            if (index > maxVolume) {
                index = maxVolume;
            } else if (index < 0) {
                index = 0;
            }
            audioManager.setStreamVolume(STREAM, index, 0);
            mIndicatorImageView.setImageResource(R.drawable.ic_volume);
            mIndicatorTextView.setText(index * 100 / maxVolume + "%");
            showIndicator();
        }

        @Override
        public Rect getPlayerViewRect() {
            return
                    new Rect(mPlayerView.getLeft(),
                            mPlayerView.getTop(),
                            mPlayerView.getRight(),
                            mPlayerView.getBottom());
        }


        @Override
        public void onDoubleTap() {
//            if (mPlayer.getPlayWhenReady()) {
//                pause();
//            } else {
//                mPlayer.setPlayWhenReady(true);
//                mPlayer.getPlaybackState();
//            }
//            mPlayerView.hideController();
        }

        @Override
        public void onGestureDone() {
            mVideoBrightness.onGestureDone();
            mStartVolumePercent = -1.0f;
            mStartVideoTime = -1;
            hideIndicator();
        }

        @Override
        public boolean onSingleTap() {
            return showOrHideHud();
        }

    }
}
/*
[onAttach]
[onCreate]
[onCreateView]
[findViews]
[onViewCreated]
[onActivityCreated]
[onViewStateRestored]
[onStart]
[onResume]
[onSurfaceTextureAvailable]
[debugPlay]
[setupPlayer]
[createPlayer]
[onPrepared]
[adjustSize]
[onPause]
[pause]
[onStop]
[onSaveInstanceState]

[videoPlaybackStopped]
[onPause]
[videoPlaybackStopped]
[onDestroyView]
[onSurfaceTextureDestroyed]
[onDestroy]
[onDetach]

* */