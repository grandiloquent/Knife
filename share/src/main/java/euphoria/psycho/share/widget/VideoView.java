package euphoria.psycho.share.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.Map;


public class VideoView extends SurfaceView {
    private static final int STATE_ERROR = -1;
    private static final int STATE_IDLE = 0;
    private static final int STATE_PAUSED = 4;
    private static final int STATE_PLAYBACK_COMPLETED = 5;
    private static final int STATE_PLAYING = 3;
    private static final int STATE_PREPARED = 2;
    private static final int STATE_PREPARING = 1;
    private static final String TAG = "TAG/" + VideoView.class.getSimpleName();
    private AudioAttributes mAudioAttributes;
    private int mAudioFocusType = AudioManager.AUDIOFOCUS_GAIN;
    private AudioManager mAudioManager;
    private int mAudioSession;
    private int mCurrentBufferPercentage;
    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };
    private int mCurrentState = STATE_IDLE;
    private Map<String, String> mHeaders;
    private MediaPlayer mMediaPlayer = null;
    private MediaPlayer.OnCompletionListener mOnCompletionListener;
    private MediaPlayer.OnErrorListener mOnErrorListener;
    private MediaPlayer.OnInfoListener mOnInfoListener;
    private MediaPlayer.OnInfoListener mInfoListener =
            new MediaPlayer.OnInfoListener() {
                public boolean onInfo(MediaPlayer mp, int arg1, int arg2) {
                    if (mOnInfoListener != null) {
                        mOnInfoListener.onInfo(mp, arg1, arg2);
                    }
                    return true;
                }
            };
    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    MediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            if (mOnSeekCompleteListener != null) mOnSeekCompleteListener.onSeekComplete(mp);
        }
    };
    private int mSeekWhenPrepared;
    private int mSurfaceHeight;
    private SurfaceHolder mSurfaceHolder = null;
    private int mSurfaceWidth;
    private int mTargetState = STATE_IDLE;
    private MediaPlayer.OnErrorListener mErrorListener =
            new MediaPlayer.OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    mCurrentState = STATE_ERROR;
                    mTargetState = STATE_ERROR;


                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    if (getWindowToken() != null) {
                        Resources r = getContext().getResources();
                        int messageId;
                        if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                            messageId = android.R.string.VideoView_error_text_invalid_progressive_playback;
                        } else {
                            messageId = android.R.string.VideoView_error_text_unknown;
                        }
                        new AlertDialog.Builder(getContext())
                                .setMessage(messageId)
                                .setPositiveButton(android.R.string.VideoView_error_button,
                                        (dialog, whichButton) -> {

                                            if (mOnCompletionListener != null) {
                                                mOnCompletionListener.onCompletion(mMediaPlayer);
                                            }
                                        })
                                .setCancelable(false)
                                .show();
                    }
                    return true;
                }
            };
    private MediaPlayer.OnCompletionListener mCompletionListener =
            new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mCurrentState = STATE_PLAYBACK_COMPLETED;
                    mTargetState = STATE_PLAYBACK_COMPLETED;

                    if (mOnCompletionListener != null) {
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                    if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
                        mAudioManager.abandonAudioFocus(null);
                    }
                }
            };
    private Uri mUri;
    private int mVideoHeight;
    private int mVideoWidth;
    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();
                    if (mVideoWidth != 0 && mVideoHeight != 0) {
                        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                        requestLayout();
                    }
                }
            };
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            boolean isValidState = (mTargetState == STATE_PLAYING);
            boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared);
                }
                start();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            openVideo();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {

            mSurfaceHolder = null;
            release(true);
        }
    };
    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            mCurrentState = STATE_PREPARED;


            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
            }

            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            int seekToPosition = mSeekWhenPrepared;
            if (seekToPosition != 0) {
                seekTo(seekToPosition);
            }
            if (mVideoWidth != 0 && mVideoHeight != 0) {

                getHolder().setFixedSize(mVideoWidth, mVideoHeight);
                if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {


                    if (mTargetState == STATE_PLAYING) {
                        start();

                    } else if (!isPlaying() &&
                            (seekToPosition != 0 || getCurrentPosition() > 0)) {

                    }
                }
            } else {


                if (mTargetState == STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mVideoWidth = 0;
        mVideoHeight = 0;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAudioAttributes = new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE).build();
        }
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        mCurrentState = STATE_IDLE;
        mTargetState = STATE_IDLE;
    }

    public int getAudioSessionId() {
        if (mAudioSession == 0) {
            MediaPlayer foo = new MediaPlayer();
            mAudioSession = foo.getAudioSessionId();
            foo.release();
        }
        return mAudioSession;
    }

    public int getCurrentPosition() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (isInPlaybackState()) {
            return mMediaPlayer.getDuration();
        }
        return -1;
    }

    private boolean isInPlaybackState() {
        return (mMediaPlayer != null &&
                mCurrentState != STATE_ERROR &&
                mCurrentState != STATE_IDLE &&
                mCurrentState != STATE_PREPARING);
    }

    public boolean isPlaying() {
        return isInPlaybackState() && mMediaPlayer.isPlaying();
    }

    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {

            return;
        }


        release(false);
        if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mAudioManager.requestAudioFocus(
                        new AudioFocusRequest
                                .Builder(AudioManager.AUDIOFOCUS_GAIN)
                                .setAudioAttributes(mAudioAttributes)
                                .build());
            } else {
                mAudioManager.requestAudioFocus(null, mAudioFocusType, AudioManager.AUDIOFOCUS_GAIN);
            }

        }
        try {
            mMediaPlayer = new MediaPlayer();


            if (mAudioSession != 0) {
                mMediaPlayer.setAudioSessionId(mAudioSession);
            } else {
                mAudioSession = mMediaPlayer.getAudioSessionId();
            }
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnInfoListener(mInfoListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mMediaPlayer.setDataSource(getContext(), mUri, mHeaders);
            mMediaPlayer.setDisplay(mSurfaceHolder);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaPlayer.setAudioAttributes(mAudioAttributes);
            }
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mMediaPlayer.setScreenOnWhilePlaying(true);
            mMediaPlayer.prepareAsync();


            mCurrentState = STATE_PREPARING;

        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            mCurrentState = STATE_ERROR;
            mTargetState = STATE_ERROR;
            mErrorListener.onError(mMediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } finally {
        }
    }

    public void pause() {
        if (isInPlaybackState()) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrentState = STATE_PAUSED;
            }
        }
        mTargetState = STATE_PAUSED;
    }

    private void release(boolean cleartargetstate) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            if (cleartargetstate) {
                mTargetState = STATE_IDLE;
            }
            if (mAudioFocusType != AudioManager.AUDIOFOCUS_NONE) {
                mAudioManager.abandonAudioFocus(null);
            }
        }
    }

    public void resume() {
        openVideo();
    }

    public void seekTo(int msec) {
        if (isInPlaybackState()) {
            mMediaPlayer.seekTo(msec);
            mSeekWhenPrepared = 0;
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        mOnSeekCompleteListener = onSeekCompleteListener;
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    public void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    public void start() {
        if (isInPlaybackState()) {
            mMediaPlayer.start();
            mCurrentState = STATE_PLAYING;
        }
        mTargetState = STATE_PLAYING;
    }

    public void stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            mCurrentState = STATE_IDLE;
            mTargetState = STATE_IDLE;
            mAudioManager.abandonAudioFocus(null);
        }
    }

    public void suspend() {
        release(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {

                width = widthSpecSize;
                height = heightSpecSize;

                if (mVideoWidth * height < width * mVideoHeight) {

                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {

                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {

                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {

                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {

                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {

                    width = widthSpecSize;
                }
            } else {

                width = mVideoWidth;
                height = mVideoHeight;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {

                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {

                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
        } else {

        }
        setMeasuredDimension(width, height);
    }
}
/*
public boolean acceptsDelayedFocusGain()
public AudioFocusRequest build()
public Builder(int focusGain)
public Builder(@NonNull AudioFocusRequest requestToCopy)
public @NonNull AudioAttributes getAudioAttributes()
public int getFocusGain()
public @Nullable OnAudioFocusChangeListener getOnAudioFocusChangeListener()
public @Nullable Handler getOnAudioFocusChangeListenerHandler()
public boolean locksFocus()
public @NonNull Builder setAcceptsDelayedFocusGain(boolean acceptsDelayedFocusGain)
public @NonNull Builder setAudioAttributes(@NonNull AudioAttributes attributes)
public @NonNull Builder setFocusGain(int focusGain)
public @NonNull Builder setForceDucking(boolean forceDucking)
public @NonNull Builder setLocksFocus(boolean focusLocked)
public @NonNull Builder setOnAudioFocusChangeListener(@NonNull OnAudioFocusChangeListener listener)
public @NonNull Builder setOnAudioFocusChangeListener(@NonNull OnAudioFocusChangeListener listener, @NonNull Handler handler)
public @NonNull Builder setWillPauseWhenDucked(boolean pauseOnDuck)
public boolean willPauseWhenDucked()

 */
/*
public int abandonAudioFocus(OnAudioFocusChangeListener l)
public int abandonAudioFocus(OnAudioFocusChangeListener l, AudioAttributes aa)
public void abandonAudioFocusForCall()
public int abandonAudioFocusRequest(@NonNull AudioFocusRequest focusRequest)
public void adjustStreamVolume(int streamType, int direction, int flags)
public void adjustSuggestedStreamVolume(int direction, int suggestedStreamType, int flags)
public static final String adjustToString(int adj)
public void adjustVolume(int direction, int flags)
public AudioManager()
public AudioManager(Context context)
public void avrcpSupportsAbsoluteVolume(String address, boolean support)
public void clearAudioServerStateCallback()
public static int createAudioPatch(AudioPatch[] patch,AudioPortConfig[] sources,AudioPortConfig[] sinks)
public void disableSafeMediaVolume()
public void dispatchAudioFocusChange(int focusChange, String id)
public int dispatchAudioFocusChange(@NonNull AudioFocusInfo afi, int focusChange,@NonNull AudioPolicy ap)
public void dispatchAudioServerStateChange(boolean state)
public void dispatchFocusResultFromExtPolicy(int requestResult, String clientId)
public void dispatchMediaKeyEvent(KeyEvent keyEvent)
public void dispatchPlaybackConfigChange(List<AudioPlaybackConfiguration> configs,boolean flush)
public void dispatchRecordingConfigChange(List<AudioRecordingConfiguration> configs)
public static String flagsToString(int flags)
public void forceVolumeControlStream(int streamType)
public int generateAudioSessionId()
public @NonNull List<AudioPlaybackConfiguration> getActivePlaybackConfigurations()
public @NonNull List<AudioRecordingConfiguration> getActiveRecordingConfigurations()
public AudioDeviceInfo[] getDevices(int flags)
public int getDevicesForStream(int streamType)
public static AudioDeviceInfo[] getDevicesStatic(int flags)
public int getFocusRampTimeMs(int focusGain, AudioAttributes attr)
public int getLastAudibleStreamVolume(int streamType)
public List<MicrophoneInfo> getMicrophones() throws IOException
public int getMode()
public int getOutputLatency(int streamType)
public String getParameters(String keys)
public String getProperty(String key)
public Map<Integer, Boolean> getReportedSurroundFormats()
public int getRingerMode()
public int getRingerModeInternal()
public IRingtonePlayer getRingtonePlayer()
public int getRouting(int mode)
public int getStreamMaxVolume(int streamType)
public int getStreamMinVolume(int streamType)
public int getStreamMinVolumeInt(int streamType)
public int getStreamVolume(int streamType)
public float getStreamVolumeDb(@PublicStreamTypes int streamType, int index,@AudioDeviceInfo.AudioDeviceTypeOut int deviceType)
public Map<Integer, Boolean> getSurroundFormats()
public int getUiSoundsStreamType()
public int getVibrateSetting(int vibrateType)
public void handleBluetoothA2dpDeviceConfigChange(BluetoothDevice device)
public void handleMessage(Message msg)
public void handleMessage(Message msg)
public boolean isAudioFocusExclusive()
public boolean isAudioServerRunning()
public boolean isBluetoothA2dpOn()
public boolean isBluetoothScoAvailableOffCall()
public boolean isBluetoothScoOn()
public boolean isHdmiSystemAudioSupported()
public static boolean isInputDevice(int device)
public boolean isMasterMute()
public boolean isMicrophoneMute()
public boolean isMusicActive()
public boolean isMusicActiveRemotely()
public boolean isOffloadedPlaybackSupported(@NonNull AudioFormat format)
public static boolean isOutputDevice(int device)
public boolean isSilentMode()
public boolean isSpeakerphoneOn()
public boolean isStreamAffectedByMute(int streamType)
public boolean isStreamAffectedByRingerMode(int streamType)
public boolean isStreamMute(int streamType)
public static boolean isValidRingerMode(int ringerMode)
public boolean isVolumeFixed()
public boolean isWiredHeadsetOn()
public static int listAudioDevicePorts(ArrayList<AudioDevicePort> devices)
public static int listAudioPatches(ArrayList<AudioPatch> patches)
public static int listAudioPorts(ArrayList<AudioPort> ports)
public static int listPreviousAudioDevicePorts(ArrayList<AudioDevicePort> devices)
public static int listPreviousAudioPorts(ArrayList<AudioPort> ports)
public void loadSoundEffects()
public static MicrophoneInfo microphoneInfoFromAudioDeviceInfo(AudioDeviceInfo deviceInfo)
public void notifyVolumeControllerVisible(IVolumeController controller, boolean visible)
public void onAudioFocusChange(int focusChange);}private static class FocusRequestInfo
public void onAudioPatchListUpdate(AudioPatch[] patchList) {}public void onServiceDied()
public void onAudioPortListUpdate(AudioPort[] portList);public void onAudioPatchListUpdate(AudioPatch[] patchList);public void onServiceDied();}public void registerAudioPortUpdateListener(OnAudioPortUpdateListener l)
public void onAudioPortListUpdate(AudioPort[] portList)
public void onAudioServerDown() { }public void onAudioServerUp()
public void onPlaybackConfigChanged(List<AudioPlaybackConfiguration> configs) {}}private static class AudioPlaybackCallbackInfo
public void onRecordingConfigChanged(List<AudioRecordingConfiguration> configs) {}}private static class AudioRecordingCallbackInfo
public void  playSoundEffect(int effectType)
public void  playSoundEffect(int effectType, int userId)
public void  playSoundEffect(int effectType, float volume)
public void preDispatchKeyEvent(KeyEvent event, int stream)
public void registerAudioDeviceCallback(AudioDeviceCallback callback,android.os.Handler handler)
public void registerAudioFocusRequest(@NonNull AudioFocusRequest afr)
public void registerAudioPlaybackCallback(@NonNull AudioPlaybackCallback cb, Handler handler)
public int registerAudioPolicy(@NonNull AudioPolicy policy)
public void registerAudioRecordingCallback(@NonNull AudioRecordingCallback cb, Handler handler)
public void registerMediaButtonEventReceiver(ComponentName eventReceiver)
public void registerMediaButtonEventReceiver(PendingIntent eventReceiver)
public void registerMediaButtonIntent(PendingIntent pi, ComponentName eventReceiver)
public void registerRemoteControlClient(RemoteControlClient rcClient)
public boolean registerRemoteController(RemoteController rctlr)
public static int releaseAudioPatch(AudioPatch patch)
public void reloadAudioSettings()
public int requestAudioFocus(OnAudioFocusChangeListener l, int streamType, int durationHint)
public int requestAudioFocus(@NonNull AudioFocusRequest focusRequest)
public int requestAudioFocus(OnAudioFocusChangeListener l,@NonNull AudioAttributes requestAttributes,int durationHint,int flags) throws IllegalArgumentException
public int requestAudioFocus(OnAudioFocusChangeListener l,@NonNull AudioAttributes requestAttributes,int durationHint,int flags,AudioPolicy ap) throws IllegalArgumentException
public int requestAudioFocus(@NonNull AudioFocusRequest afr, @Nullable AudioPolicy ap)
public void requestAudioFocusForCall(int streamType, int durationHint)
public void safeNotify()
public void safeWait(long millis) throws InterruptedException
public static int setAudioPortGain(AudioPort port, AudioGainConfig gain)
public void setAudioServerStateCallback(@NonNull Executor executor,@NonNull AudioServerStateCallback stateCallback)
public int setBluetoothA2dpDeviceConnectionState(BluetoothDevice device, int state,int profile)
public int setBluetoothA2dpDeviceConnectionStateSuppressNoisyIntent(BluetoothDevice device, int state, int profile,boolean suppressNoisyIntent, int a2dpVolume)
public void setBluetoothScoOn(boolean on)
public void setFocusRequestResult(@NonNull AudioFocusInfo afi,@FocusRequestResult int requestResult, @NonNull AudioPolicy ap)
public int setHdmiSystemAudioSupported(boolean on)
public void setHearingAidDeviceConnectionState(BluetoothDevice device, int state)
public void setMasterMute(boolean mute, int flags)
public void setMicrophoneMute(boolean on)
public void setMode(int mode)
public void setParameters(String keyValuePairs)
public static void setPortIdForMicrophones(ArrayList<MicrophoneInfo> microphones)
public void setRingerMode(int ringerMode)
public void setRingerModeInternal(int ringerMode)
public void setRouting(int mode, int routes, int mask)
public void setSpeakerphoneOn(boolean on)
public void setStreamMute(int streamType, boolean state)
public void setStreamSolo(int streamType, boolean state)
public void setStreamVolume(int streamType, int index, int flags)
public boolean setSurroundFormatEnabled(@AudioFormat.SurroundSoundEncoding int audioFormat, boolean enabled)
public void setVibrateSetting(int vibrateType, int vibrateSetting)
public void setVolumeController(IVolumeController controller)
public void setVolumePolicy(VolumePolicy policy)
public void setWiredDeviceConnectionState(int type, int state, String address, String name)
public boolean shouldVibrate(int vibrateType)
public void startBluetoothSco()
public void startBluetoothScoVirtualCall()
public void stopBluetoothSco()
public void unloadSoundEffects()
public void unregisterAudioDeviceCallback(AudioDeviceCallback callback)
public void unregisterAudioFocusRequest(OnAudioFocusChangeListener l)
public void unregisterAudioPlaybackCallback(@NonNull AudioPlaybackCallback cb)
public void unregisterAudioPolicyAsync(@NonNull AudioPolicy policy)
public void unregisterAudioPortUpdateListener(OnAudioPortUpdateListener l)
public void unregisterAudioRecordingCallback(@NonNull AudioRecordingCallback cb)
public void unregisterMediaButtonEventReceiver(ComponentName eventReceiver)
public void unregisterMediaButtonEventReceiver(PendingIntent eventReceiver)
public void unregisterMediaButtonIntent(PendingIntent pi)
public void unregisterRemoteControlClient(RemoteControlClient rcClient)
public void unregisterRemoteController(RemoteController rctlr)
public void waitForResult(long timeOutMs)


 */