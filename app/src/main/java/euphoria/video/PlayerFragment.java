package euphoria.video;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection.Factory;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import euphoria.psycho.knife.R;

public class PlayerFragment extends ImmersiveModeFragment {
    private PlayerDelegate mPlayerDelegate;
    PlayerView mPlayerView;
    PlayerViewGestureHandler mPlayerViewGestureHandler;
    ProgressBar mLoadingVideoView;
    SimpleExoPlayer mPlayer;

    private SimpleExoPlayer createExoPlayer() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        Context context = getContext();
        DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(context);
        return ExoPlayerFactory.newSimpleInstance(getContext(), defaultRenderersFactory, trackSelector, new DefaultLoadControl(), null, bandwidthMeter);
    }

    public int getCurrentVideoPosition() {
        return (int) mPlayer.getCurrentPosition();
    }

    private void initializeViews(View view) {
//        Toolbar toolbar = view.findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPlayerView = view.findViewById(R.id.player_view);
        mPlayerViewGestureHandler.initView(view);
        mPlayerView.setOnTouchListener(mPlayerViewGestureHandler);
        mPlayerView.requestFocus();


        setupPlayer();
        mPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        mLoadingVideoView = view.findViewById(R.id.loadingVideoView);
    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    private synchronized void setupPlayer() {
        if (mPlayerView.getPlayer() == null) {
            if (mPlayer == null) {
                mPlayer = createExoPlayer();
            }
            mPlayer.addListener(new EventListener() {
                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                }
            });
            mPlayer.setPlayWhenReady(true);
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mPlayerView.setPlayer(mPlayer);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        mPlayerDelegate = (PlayerDelegate) activity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        hideNavigationBar();
        mPlayerViewGestureHandler = new PlayerViewGestureHandler();

        View view = inflater.inflate(R.layout.fragment_player, container, false);
        setHasOptionsMenu(true);
        initializeViews(view);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mPlayerView.setPlayer(null);
    }

    public interface PlayerDelegate {

    }

    private static class VideoBrightness {
        private static final String BRIGHTNESS_LEVEL_PREF = "brightness";
        private float mBrightness;
        private float mInitialBrightness;
        private Activity mActivity;


        public VideoBrightness(final Activity activity) {
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

        public String getBrightnessString() {
            return ((int) (mBrightness * 100)) + "%";
        }

        private void loadBrightnessFromPreference() {
            final float brightnessPref = PreferenceManager.getDefaultSharedPreferences(mActivity)
                    .getFloat(BRIGHTNESS_LEVEL_PREF, 1f);
            setBrightness(brightnessPref);

        }

        public void onGestureDone() {
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

        public void setVideoBrightness(double adjustPercent) {
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

        public PlayerViewGestureHandler() {
            super(getContext());
            mVideoBrightness = new VideoBrightness(getActivity());
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

        private void hideIndicator() {
            mIndicatorView.setVisibility(View.GONE);
        }

        public void initView(View view) {
            mIndicatorView = view.findViewById(R.id.indicatorView);
            mIndicatorImageView = view.findViewById(R.id.indicatorImageView);
            mIndicatorTextView = view.findViewById(R.id.indicatorTextView);

            mPlayerView.setControllerVisibilityListener(visibility -> mIsControllerVisible = (visibility == View.VISIBLE));
        }

        private void showIndicator() {
            mIndicatorView.setVisibility(View.VISIBLE);
        }

        private boolean showOrHideHud() {
            if (mIsControllerVisible) {
                mPlayerView.hideController();
                hideNavigationBar();
            } else {
                mPlayerView.showController();
                showNavigationBar();
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
            long totalTime = mPlayer.getDuration();
            if (adjustPercent < -1.0f) {
                adjustPercent = -1.0f;
            } else if (adjustPercent > 1.0f) {
                adjustPercent = 1.0f;
            }
            if (mStartVideoTime < 0) {
                mStartVideoTime = mPlayer.getCurrentPosition();
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
            mPlayer.seekTo(targetTime);
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
            if (mPlayer.getPlayWhenReady()) {
                pause();
            } else {
                mPlayer.setPlayWhenReady(true);
                mPlayer.getPlaybackState();
            }
            mPlayerView.hideController();
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

        @Override
        public void onVideoDescriptionGesture() {

        }
    }
}
