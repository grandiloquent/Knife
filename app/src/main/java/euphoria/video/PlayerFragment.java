package euphoria.video;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import euphoria.common.Dialogs;
import euphoria.common.Logger;
import euphoria.common.Strings;
import euphoria.psycho.knife.R;

import static euphoria.common.Files.getDirectoryName;
import static euphoria.common.Files.read;

public class PlayerFragment extends ImmersiveModeFragment implements
        PlayerFragmentDelegate,
        Player.EventListener {
    public static final String EXTRA_DIRECTION = "extra_direction";
    public static final String EXTRA_SORT = "extra_sort";
    public static final String EXTRA_VIDEO_PATH = "extra_video_path";
    private PlayerDelegate mPlayerDelegate;
    private PlayerView mPlayerView;
    private PlayerViewGestureHandler mPlayerViewGestureHandler;
    private ProgressBar mLoadingVideoView;
    private SimpleExoPlayer mPlayer;
    private ConcatenatingMediaSource mConcatenatingMediaSource;
    private List<String> mFiles;
    private Bookmarker mBookmarker;
    private int mWindow;
    private String mVideoDirectory;
    private int mSort;
    private boolean mIsSortByAscending;

    /*删除视频文件*/
    private void actionDelete() {
        String currentPath = getCurrentPath();
        String message = String.format("确定删除要 %s 吗?", Strings.substringAfterLast(currentPath, "/"));
        Dialogs.showConfirmDialog(getContext(),
                null, message, (dialog, which) -> {
                    dialog.dismiss();
                    pause();
                    boolean performResult = performDelete(currentPath);
                    if (performResult) {
                        getActivityChecked().setResult(Activity.RESULT_OK);
                    }
                }
        );

    }

    /*设置对应的播放索引*/
    private void anchorPlayIndex(String path) {
        mWindow = mFiles.indexOf(path);
    }

    private Stream<Path> collectFiles() throws IOException {
       /* Log.e("TAG/" + PlayerFragment.this.getClass().getSimpleName(), "Debug: collectFiles, "
                + " path = " + path + "\n" + " sort = " + sort + "\n" + " isAscending = " + isAscending + "\n");
                */
        Pattern extension = Pattern.compile("\\.(?:3gp|mkv|mp4|ts|webm)$", Pattern.CASE_INSENSITIVE);
        return Files.list(Paths.get(mVideoDirectory))
                .filter(p ->
                        Files.isRegularFile(p)
                                && extension.matcher(p.getFileName().toString()).find())
                .sorted(new VideoComparator(mSort));
    }

    private void collectPlaylist() throws IOException {
        Stream<Path> files = collectFiles();
        FileDataSourceFactory factory = new FileDataSourceFactory();
        if (mConcatenatingMediaSource == null) {
            mConcatenatingMediaSource = new ConcatenatingMediaSource();
        } else {
            mConcatenatingMediaSource.clear();
        }
        if (mFiles == null) {
            mFiles = new ArrayList<>();
        } else {
            mFiles.clear();
        }
        files.forEach(p -> {
            mFiles.add(p.toAbsolutePath().toString());
            MediaSource mediaSource = new ExtractorMediaSource.Factory(factory).createMediaSource(Uri.fromFile(p.toFile()));
            mConcatenatingMediaSource.addMediaSource(mediaSource);
        });
    }

    private SimpleExoPlayer createExoPlayer() {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory();
        DefaultTrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        Context context = getContext();
        DefaultRenderersFactory defaultRenderersFactory = new DefaultRenderersFactory(context);
        return ExoPlayerFactory.newSimpleInstance(getContext(), defaultRenderersFactory, trackSelector, new DefaultLoadControl(), null, bandwidthMeter);
    }

    private Activity getActivityChecked() {
        Activity activity = getActivity();
        if (activity == null) {
            Logger.e(this, "getActivity() return null.\n");
            throw new NullPointerException();
        }
        return activity;
    }

    /*获取当前播放的文件路径*/
    private String getCurrentPath() {
        int index = mPlayer.getCurrentWindowIndex();
        return mFiles.get(index);
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

    private boolean performDelete(String videoPath) {
        File videoFile = new File(videoPath);
        boolean deleteResult = videoFile.delete();
        if (!deleteResult) return false;
        mConcatenatingMediaSource.removeMediaSource(mPlayer.getCurrentPeriodIndex());
//        if (mFiles.size() == 1) {
//            mConcatenatingMediaSource = null;
//            mPlayer.stop(true);
//            return true;
//        }
//        int currentWindowIndex = mPlayer.getCurrentWindowIndex();
//        int nextWindowIndex = currentWindowIndex + 1;
//        if (nextWindowIndex + 1 >= mFiles.size()) {
//            nextWindowIndex = 0;
//        }
//        String mNextVideoPath = mFiles.get(nextWindowIndex);
//        try {
//            collectPlaylist();
//        } catch (IOException e) {
//            return false;
//        }
//        anchorPlayIndex(mNextVideoPath);
//        mPlayer.prepare(mConcatenatingMediaSource);
//        mPlayer.seekTo(mWindow, C.TIME_UNSET);
//        mPlayer.setPlayWhenReady(true);
        return true;
    }

    private void playVideo() {
        if (mConcatenatingMediaSource == null) {
            Intent start = Objects.requireNonNull(getActivity()).getIntent();
            String videoPath = start.getStringExtra(EXTRA_VIDEO_PATH);
            int sort = start.getIntExtra(EXTRA_SORT, -1);
            boolean isAscending = start.getBooleanExtra(EXTRA_DIRECTION, false);
            mVideoDirectory = getDirectoryName(videoPath);
            mSort = sort;
            mIsSortByAscending = isAscending;
            try {
                collectPlaylist();
            } catch (IOException e) {
                Logger.e(this, "collectPlaylist failed. %s\n", e.getMessage());
            }
            anchorPlayIndex(videoPath);
        }
        mPlayer.prepare(Objects.requireNonNull(mConcatenatingMediaSource));
        mPlayer.seekTo(mWindow, C.TIME_UNSET);
    }

    private void preventDeviceSleeping(boolean flag) {
        // prevent the device from sleeping while playing
        Activity activity = getActivity();
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                if (flag) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        }
    }

    private void seekToLasted() {
        Integer position = mBookmarker.getBookmark(mFiles.get(mPlayer.getCurrentWindowIndex()));
        if (position != null) mPlayer.seekTo(mPlayer.getCurrentWindowIndex(), position);
    }

    private void setTitle() {
        getSupportActionBar().setTitle(Strings.substringAfterLast(getCurrentPath(), "/"));
    }

    private synchronized void setupPlayer() {
        if (mPlayerView.getPlayer() == null) {
            if (mPlayer == null) {
                mPlayer = createExoPlayer();
            }
            mPlayer.addListener(this);
            mPlayer.setPlayWhenReady(true);
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mPlayerView.setPlayer(mPlayer);
        }
    }

    @Override
    public int getCurrentVideoPosition() {
        return (int) mPlayer.getCurrentPosition();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        mPlayerDelegate = (PlayerDelegate) activity;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.video, menu);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        hideNavigationBar();
        mPlayerViewGestureHandler = new PlayerViewGestureHandler();
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        setHasOptionsMenu(true);
        initializeViews(view);
        mBookmarker = new Bookmarker(getContext());
        playVideo();
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlayer.stop();
        mPlayer.release();
        mPlayer = null;
        mPlayerView.setPlayer(null);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (isLoading) {
            mLoadingVideoView.setVisibility(View.VISIBLE);
        } else {
            mLoadingVideoView.setVisibility(View.GONE);
        }
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
        super.onPause();
        pause();
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

        Logger.e(PlayerFragment.this, "onPlayerError. %s.\n", error.getMessage());

        Toast.makeText(getContext(),
                String.format("ExoPlaybackException:\n%s\n\n"
                                + "RendererException:\n%s\n\n",
                        error.toString(),
                        error.getRendererException().toString()),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY && playWhenReady) {
            preventDeviceSleeping(true);
        } else {
            preventDeviceSleeping(false);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
        setTitle();
        seekToLasted();
    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
        mBookmarker.setBookmark(getCurrentPath(), (int) mPlayer.getCurrentPosition());
    }

    @Override
    public void videoPlaybackStopped() {
        mPlayer.stop();
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

    private static class VideoComparator implements Comparator<Path> {
        public static final int SORT_BY_UNSPECIFIED = -1;
        static final int SORT_BY_DATE_MODIFIED = 2;
        static final int SORT_BY_NAME = 0;
        static final int SORT_BY_SIZE = 1;
        private final int mSort;

        private VideoComparator(int sort) {
            mSort = sort;
        }

        @Override
        public int compare(Path o2, Path o1) {
            switch (mSort) {
                case SORT_BY_SIZE:
                    long result = o1.toFile().length() - o2.toFile().length();
                    if (result < 0) {
                        return -1;
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                case SORT_BY_NAME:
                    Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.compare(o1.getFileName(),
                            o2.getFileName());
                case SORT_BY_DATE_MODIFIED:
                    result =
                            o1.toFile().lastModified()
                                    - o2.toFile().lastModified();
                    if (result < 0) {
                        return -1;
                    } else if (result > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                default:
                    return 0;
            }
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

        void initView(View view) {
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