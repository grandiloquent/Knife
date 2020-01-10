package euphoria.video;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import euphoria.common.Files;
import euphoria.psycho.knife.R;
import euphoria.video.TimeBar.OnScrubListener;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayerFragment extends ImmersiveModeFragment implements
        SurfaceTextureListener,
        PlayerFragmentDelegate,
        OnPreparedListener,
        OnScrubListener,
        OnCompletionListener,
        OnClickListener {
    public static final String EXTRA_DIRECTION = "extra_direction";
    public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_VIDEO_PATH = "extra_video_path";
    private static final int HIDE_CONTROLLER_DELAY = 5000;
    private final Handler mHandler = new Handler();
    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenHeightDifference;
    private SurfaceTexture mSurfaceTexture;
    private IjkMediaPlayer mIjkMediaPlayer;
    private Handler mProgressHandler;
    boolean mDragging;
    boolean mShowing = true;
    private View mController;
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
    private TextView mExoDuration;
    private View mExoFfwd;
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
    private View mExoRew;
    private View mIndicatorImageView;
    private View mIndicatorTextView;
    private View mIndicatorView;
    private View mLoadingVideoView;
    private View mPlayerView;
    private TextureView mTextureView;
    private PlayerViewGestureHandler mPlayerViewGestureHandler;

    private void adjustSize() {
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

    private IjkMediaPlayer createPlayer() {
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);
        return ijkMediaPlayer;
    }

    private void debugPlay() {
        String videoDirectory = Files.getExternalStoragePath("videos");
        File[] videoFiles = Files.listVideoFiles(videoDirectory);

        setupPlayer();

        try {
            mIjkMediaPlayer.setDataSource(videoFiles[3].getAbsolutePath());
            mIjkMediaPlayer.prepareAsync();
        } catch (IOException e) {

            Log.e("TAG/" + PlayerFragment.this.getClass().getSimpleName(), "Error: debugPlay, " + e.getMessage() + " " + e.getCause());

        }
    }

    private void findViews(View view) {
        mController = view.findViewById(R.id.controller);
        mExoDuration = view.findViewById(R.id.exo_duration);
        mExoFfwd = view.findViewById(R.id.exo_ffwd);
        mExoNext = view.findViewById(R.id.exo_next);
        mExoPause = view.findViewById(R.id.exo_pause);
        mExoPlay = view.findViewById(R.id.exo_play);
        mExoPosition = view.findViewById(R.id.exo_position);
        mExoPrev = view.findViewById(R.id.exo_prev);
        mExoProgress = view.findViewById(R.id.exo_progress);
        mExoRew = view.findViewById(R.id.exo_rew);
        mIndicatorImageView = view.findViewById(R.id.indicatorImageView);
        mIndicatorTextView = view.findViewById(R.id.indicatorTextView);
        mIndicatorView = view.findViewById(R.id.indicatorView);
        mLoadingVideoView = view.findViewById(R.id.loadingVideoView);
        mPlayerView = view.findViewById(R.id.player_view);
        mTextureView = view.findViewById(R.id.texture_view);
    }

    private String formatDuration(long duration) {
        long h = duration / 3600;
        long m = (duration - h * 3600) / 60;
        long s = duration - (h * 3600 + m * 60);
        String durationValue;
        if (h == 0) {
            durationValue = String.format(Locale.getDefault(), "%1$02d:%2$02d", m, s);
        } else {
            durationValue = String.format(Locale.getDefault(), "%1$d:%2$02d:%3$02d", h, m, s);
        }
        return durationValue;
    }

    private void hideController() {


        Log.e("TAG/", "Debug: hideController, \n"
                + " mScreenHeightDifference = " + mScreenHeightDifference + "\n"

        );
        mHandler.removeCallbacks(null);
        hideNavigationBar();
        mController.setVisibility(View.GONE);
        mShowing = false;

    }

    private void play() {
        if (mIjkMediaPlayer != null && mIjkMediaPlayer.isPlayable()) {
            mIjkMediaPlayer.start();
            mExoPlay.setVisibility(View.GONE);
            mExoPause.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mProgressChecker);
            mHandler.post(mProgressChecker);
        }
    }

    private void rewind() {
    }

    private long setProgress() {
        if (mIjkMediaPlayer == null || mDragging || !mShowing) {
            return 0;
        }
        long position = mIjkMediaPlayer.getCurrentPosition();

        mExoPosition.setText(formatDuration(position / 1000));
        mExoProgress.setPosition(position);
        return position / 1000;
    }

    private void setupPlayer() {
        IjkMediaPlayer ijkMediaPlayer = createPlayer();
        ijkMediaPlayer.setSurface(new Surface(mSurfaceTexture));
        ijkMediaPlayer.setOnPreparedListener(this);
        ijkMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mIjkMediaPlayer = ijkMediaPlayer;
    }

    private void showController() {

        showNavigationBar();
        mController.setVisibility(View.VISIBLE);

        mHandler.removeCallbacks(null);
        mHandler.post(mProgressChecker);
        mHandler.postDelayed(mPlayingChecker, HIDE_CONTROLLER_DELAY);

        mShowing = true;

    }

    @Override
    public int getCurrentVideoPosition() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exo_pause:
                pause();
                return;
            case R.id.exo_play:
                play();
                return;
            case R.id.exo_rew:
                rewind();
                return;
        }
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        mHandler.removeCallbacks(null);
        mExoDuration.setText(null);
        mExoPosition.setText(null);
        mExoProgress.setPosition(0);
    }

    /*Starting at here*/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mProgressHandler = new Handler();

        View view = inflater.inflate(R.layout.fragment_player, container, false);

        findViews(view);

        mExoRew.setVisibility(View.GONE);
        mExoFfwd.setVisibility(View.GONE);

        mPlayerViewGestureHandler = new PlayerViewGestureHandler();
        mPlayerViewGestureHandler.initView(mPlayerView);
        mPlayerView.setOnTouchListener(mPlayerViewGestureHandler);

        mTextureView.setSurfaceTextureListener(this);

        return view;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        adjustSize();
        iMediaPlayer.start();

        Log.e("TAG/", "Debug: onPrepared, "
                + " iMediaPlayer.getDuration() = " + iMediaPlayer.getDuration() + "\n"
        );
        mExoProgress.setDuration(iMediaPlayer.getDuration());
        mExoPosition.setText(formatDuration(0));
        mExoDuration.setText(formatDuration(iMediaPlayer.getDuration() / 1000));

        mHandler.removeCallbacks(null);
        mHandler.post(mProgressChecker);
        mHandler.postDelayed(mPlayingChecker, HIDE_CONTROLLER_DELAY);

        mExoPlay.setVisibility(View.GONE);
        mExoPause.setVisibility(View.VISIBLE);

        //mIndicatorView.setVisibility(View.GONE);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {

    }

    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        mDragging = true;
    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        mIjkMediaPlayer.seekTo(position);
        mDragging = false;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        if (mScreenWidth == 0) {
            mScreenHeight = height;
            mScreenWidth = width;
        }
        mSurfaceTexture = surface;
        debugPlay();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {

        Log.e("TAG/", "Debug: onSurfaceTextureDestroyed, ");

        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        adjustSize();
        Log.e("TAG/", "Debug: onSurfaceTextureSizeChanged, "
                + " width = " + width + "\n" + " height = " + height + "\n"

        );

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {


    }

    @Override
    public void pause() {
        if (mIjkMediaPlayer != null && mIjkMediaPlayer.isPlaying()) {
            mIjkMediaPlayer.pause();
            mExoPause.setVisibility(View.GONE);
            mExoPlay.setVisibility(View.VISIBLE);
            mHandler.removeCallbacks(mProgressChecker);
        }
    }

    @Override
    public void videoPlaybackStopped() {

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
            String targetTimeString = formatDuration(targetTime / 1000);
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
        public void onCommentsGesture() {
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


            Log.e("TAG/", "Debug: onSingleTap, \n");


            return showOrHideHud();
        }

        @Override
        public void onVideoDescriptionGesture() {
        }
    }
}
