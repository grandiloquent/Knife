package euphoria.psycho.knife;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import android.widget.RemoteViews;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import euphoria.common.Strings;

import static euphoria.common.Files.getDirectoryName;

public class MusicService extends Service {
    public static final String ACTION_NEXT = "euphoria.psycho.knife.ACTION_NEXT";
    public static final String ACTION_PLAY = "euphoria.psycho.knife.ACTION_PLAY";
    public static final String ACTION_PREVIOUS = "euphoria.psycho.knife.ACTION_PREVIOUS";
    public static final String ACTION_STOP = "euphoria.psycho.knife.ACTION_STOP";
    public static final String ACTION_TOGGLE_PAUSE = "euphoria.psycho.knife.ACTION_TOGGLE_PAUSE";
    public static final String EXTRA_PATH = "extra_path";
    private static final int FADEDOWN = 1 << 1;
    private static final int FADEUP = 1 << 2;
    private static final int FOCUS_CHANGED = 1 << 3;
    private static final int PLAYSTATE_CHANGED = 1 << 5;
    private static final int RELEASE_WAKELOCK = 1 << 6;
    private static final int SERVER_DIED = 1 << 10;
    private static final String TAG = "TAG/" + MusicService.class.getSimpleName();
    private static final int TRACK_ENDED = 1 << 11;
    private static final int TRACK_WENT_TO_NEXT = 1 << 12;
    private static String[] sAudioExtensions;

    static {
        sAudioExtensions = new String[]{
                ".mp3",
                ".m4a"
        };
    }

    private PowerManager.WakeLock mWakeLock;
    private float mCurrentVolume;
    private MultiPlayer mPlayer;
    private int mPosition;
    private int mNextPosition;
    private boolean mIsSupposedToBePlaying;
    private boolean mPaused;
    private Handler mPlayerHandler;
    private AudioManager mAudioManager;
    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener;
    private HandlerThread mHandlerThread;
    private boolean mIsServiceInUse = false;
    private List<MediaItem> mPlayList = new ArrayList<>();
    private NotificationHelper mNotificationHelper;
    private int mStartServiceId = -1;

    private void generatePlayList(String path) {
        File[] files = listAudioFiles(new File(getDirectoryName(path)));
        if (files != null && files.length > 0) {
            for (File f : files) {
                mPlayList.add(getMediaItem(f));
            }
            final Collator collator = Collator.getInstance(Locale.CHINA);
            Collections.sort(mPlayList, (o1, o2) -> collator.compare(o1.title, o2.title));
        }
    }

    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    private int getIndexOf(String path) {
        for (int i = 0; i < mPlayList.size(); i++) {
            if (mPlayList.get(i).path.equals(path)) {
                return i;
            }
        }
        return -1;
    }

    private void handleFocusChange(int which) {
        switch (which) {
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (isPlaying()) {
                    mPaused = which == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                }
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mPlayerHandler.removeMessages(FADEUP);
                mPlayerHandler.sendEmptyMessage(FADEDOWN);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                if (!isPlaying() && mPaused) {
                    mPaused = false;
                    mCurrentVolume = 0f;
                    mPlayer.setVolume(0f);
                    play();
                } else {
                    mPlayerHandler.removeMessages(FADEDOWN);
                    mPlayerHandler.sendEmptyMessage(FADEUP);
                }
                break;
        }
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        assert action != null;
        if (action.equals(ACTION_TOGGLE_PAUSE)) {
            if (isPlaying()) {
                pause();
                mPaused = false;
            } else {

                play();
            }
        } else if (action.equals(ACTION_PLAY)) {
            mPlayer.setDataSource(mPlayList.get(mPosition).path);
            play();
        } else if (action.equals(ACTION_NEXT)) {
            next();
        }
    }

    private boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    private void next() {

        if (!mPlayer.isInitialized()) {

            mPlayer.setDataSource(mPlayList.get(mPosition).path);
        } else {
            stop(false);
            if (mPosition + 1 < mPlayList.size()) {
                mPosition = mPosition + 1;
            } else {
                mPosition = 0;
            }
            mPlayer.setDataSource(mPlayList.get(mPosition).path);
            setNextTrack();
        }
        play();
    }

    private void notifyChanged(int which) {
        switch (which) {
            case PLAYSTATE_CHANGED:
                mNotificationHelper.updatePlayState(isPlaying());
                break;
        }
    }

    public void pause() {
        synchronized (this) {
            mPlayerHandler.removeMessages(FADEUP);
            if (mIsSupposedToBePlaying) {
                mPlayer.pause();
                mIsSupposedToBePlaying = false;
                notifyChanged(PLAYSTATE_CHANGED);
            }
        }
    }

    private void play() {
        int status = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }
        if (mPlayer.isInitialized()) {
            setNextTrack();

            mPlayer.start();
            mPlayerHandler.removeMessages(FADEDOWN);
            mPlayerHandler.sendEmptyMessage(FADEUP);
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChanged(PLAYSTATE_CHANGED);
            }
            updateNotification();
        }
    }

    private void releaseService() {
        if (isPlaying() || mPlayerHandler.hasMessages(TRACK_ENDED)) {
            return;
        }
        mNotificationHelper.killNotification();
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        if (!mIsServiceInUse) {
            stopSelf(mStartServiceId);
        }
    }

    private void setNextTrack() {
        if (mPosition + 1 < mPlayList.size()) {
            mNextPosition = mPosition + 1;
        } else {
            mNextPosition = 0;
        }
        mPlayer.setNextDataSource(mPlayList.get(mNextPosition).path);
    }

    private void setupAudioManager() {
        Context context;
        String name;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                mPlayerHandler.obtainMessage(FOCUS_CHANGED, focusChange, 0).sendToTarget();
            }
        };
    }

    private void setupHandler() {
        mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mPlayerHandler = new MusicPlayerHandler(this, mHandlerThread.getLooper());
    }

    private void setupPlayer() {
        mPlayer = new MultiPlayer(this);
        mPlayer.setHandler(mPlayerHandler);
    }

    private void setupWakeLock() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        }
        mWakeLock.setReferenceCounted(false);
    }

    private void stop(boolean isGoToIdle) {
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        if (isGoToIdle) {
            mIsSupposedToBePlaying = false;
        } else {
            stopForeground(false);
        }
    }

    private void updateNotification() {
        if (isPlaying()) {
            MediaItem item = mPlayList.get(mPosition);
            mNotificationHelper.buildNotification(item.title, item.artist, null, true);
        }
    }

    public static boolean endWiths(String value, String suffix, boolean ignoreCase) {
        int s = suffix.length();
        int v = value.length();
        if (v < s) return false;
        int i = 0;
        while (--s >= 0) {
            if (ignoreCase) {
                i++;
                int a = value.charAt(v - i);
                int b = suffix.charAt(s);
                a |= 0x20;
                b |= 0x20;
                if (a != b) return false;
            } else {
                if (value.charAt(v - s) != suffix.charAt(s)) return false;
            }
        }
        return true;
    }

    public static MediaItem getMediaItem(File path) {
        MediaItem item = new MediaItem();
        item.path = path.getAbsolutePath();
        item.title = Strings.substringBeforeLast(path.getName(),".");
//        if (path.isDirectory()) {
//            item.isBrowseable = true;
//            item.title = path.getName();
//            return item;
//        }
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        retriever.setDataSource(path.getAbsolutePath());
//        item.title = getFileName(path.getAbsolutePath());
////        item.title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
////        if (isEmpty(item.title)) {
////            item.title = getFileName(path.getAbsolutePath());
////        }
//        item.artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//        item.duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//        item.isBrowseable = false;
//        byte[] bitmap = retriever.getEmbeddedPicture();
//        if (bitmap != null) {
//            item.bitmap = BitmapFactory.decodeByteArray(bitmap, 0, bitmap.length);
//        }
        //        int[] keyArray = new int[]{
//                MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER,
//                /*1*/  MediaMetadataRetriever.METADATA_KEY_ALBUM,
//                /*2*/    MediaMetadataRetriever.METADATA_KEY_ARTIST,
//                MediaMetadataRetriever.METADATA_KEY_AUTHOR,
//                MediaMetadataRetriever.METADATA_KEY_COMPOSER,
//                MediaMetadataRetriever.METADATA_KEY_DATE,
//                MediaMetadataRetriever.METADATA_KEY_GENRE,
//                /*7*/ MediaMetadataRetriever.METADATA_KEY_TITLE,
//                MediaMetadataRetriever.METADATA_KEY_YEAR,
//                /*9*/   MediaMetadataRetriever.METADATA_KEY_DURATION,
//                /*10*/     MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS,
//                MediaMetadataRetriever.METADATA_KEY_WRITER,
//                /*12*/ MediaMetadataRetriever.METADATA_KEY_MIMETYPE,
//                MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST,
//                MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER,
//                MediaMetadataRetriever.METADATA_KEY_COMPILATION,
//                /*16*/   MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO,
//                MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT,
//                /*20*/ MediaMetadataRetriever.METADATA_KEY_BITRATE,
//                MediaMetadataRetriever.METADATA_KEY_LOCATION,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION,
//                MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE,
//                MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_COUNT,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_PRIMARY,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT,
//                MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION,
//                MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT,
//        };
//
//        for (int i = 0; i < keyArray.length; i++) {
//            String meta = retriever.extractMetadata(keyArray[i]);
//            Log.e(TAG, keyArray[i] + ": " + meta);
//        }
        return item;
    }

    public static boolean isAudioFile(File file) {
        String name = file.getName();
        for (String sAudioExtension : sAudioExtensions) {
            if (endWiths(name, sAudioExtension, true)) {
                return true;
            }
        }
        return false;
    }

    public static File[] listAudioFiles(File dir) {
        return dir.listFiles(pathname -> pathname.isFile() && isAudioFile(pathname));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        mIsServiceInUse = true;
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationHelper = new NotificationHelper(this);
        setupHandler();
        setupAudioManager();
        setupPlayer();
        setupWakeLock();
    }

    @Override
    public void onDestroy() {
        // Called by the system to notify a Service that it is no longer used and is being removed.
        super.onDestroy();
        final Intent audioEffectsIntent = new Intent(
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(audioEffectsIntent);
        mPlayerHandler.removeCallbacksAndMessages(null);
        mPlayer.release();
        mPlayer = null;
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        mPlayerHandler.removeCallbacksAndMessages(null);
        mWakeLock.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartServiceId = startId;
        if (intent != null) {
            String path = intent.getStringExtra(EXTRA_PATH);
            if (!TextUtils.isEmpty(path)) {
                mPlayList.clear();
                generatePlayList(path);
                mPosition = getIndexOf(path);
            }
            handleIntent(intent);
        }
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mIsServiceInUse = false;
// Called when all clients have disconnected from a particular interface  published by the service.
        return super.onUnbind(intent);
    }

    private static final class MultiPlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
        private final WeakReference<MusicService> mService;
        private MediaPlayer mCurrentPlayer = new MediaPlayer();
        private Handler mHandler;
        private boolean mIsInitialized = false;
        private MediaPlayer mNextPlayer;

        public MultiPlayer(MusicService service) {
            mService = new WeakReference<>(service);
            mCurrentPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
        }

        public long duration() {
            return mCurrentPlayer.getDuration();
        }

        public int getAudioSessionId() {
            /**
             * Returns the audio session ID.
             *
             * @return the audio session ID. {@see #setAudioSessionId(int)}
             * Note that the audio session ID is 0 only if a problem occured when the MediaPlayer was contructed.
             */
            return mCurrentPlayer.getAudioSessionId();
        }

        public void setAudioSessionId(int sessionId) {
            mCurrentPlayer.setAudioSessionId(sessionId);
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }

        public void pause() {
            mCurrentPlayer.pause();
        }

        public long position() {
            return mCurrentPlayer.getCurrentPosition();
        }

        public void release() {
            stop();
            mCurrentPlayer.release();
        }

        public long seek(long where) {
            mCurrentPlayer.seekTo((int) where);
            return where;
        }

        public void setDataSource(String path) {
            mIsInitialized = setDataSourceImpl(mCurrentPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        private boolean setDataSourceImpl(MediaPlayer player, String path) {
            try {
                player.reset();
                player.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    player.setDataSource(mService.get(), Uri.parse(path));
                } else {
                    player.setDataSource(path);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    player.setAudioAttributes(new AudioAttributes.Builder()
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());
                } else {
                    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                player.prepare();
            } catch (Exception e) {
                return false;
            }
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            /**
             *  Intent to signal to the effect control application or service that a new audio session
             *  is opened and requires audio effects to be applied.
             *  <p>This is different from {@link #ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL} in that no
             *  UI should be displayed in this case. Music player applications can broadcast this intent
             *  before starting playback to make sure that any audio effect settings previously selected
             *  by the user are applied.
             *  <p>The effect control application receiving this intent will look for previously stored
             *  settings for the calling application, create all required audio effects and apply the
             *  effect settings to the specified audio session.
             *  <p>The calling package name is indicated by the {@link #EXTRA_PACKAGE_NAME} extra and the
             *  audio session ID by the {@link #EXTRA_AUDIO_SESSION} extra. Both extras are mandatory.
             *  <p>If no stored settings are found for the calling application, default settings for the
             *  content type indicated by {@link #EXTRA_CONTENT_TYPE} will be applied. The default settings
             *  for a given content type are platform specific.
             */
            final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mService.get().getPackageName());
            mService.get().sendBroadcast(intent);
            return true;
        }

        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        public void setNextDataSource(String path) {
            try {
                mCurrentPlayer.setNextMediaPlayer(null);
            } catch (IllegalArgumentException e) {
            } catch (IllegalStateException e) {
                return;
            }
            if (mNextPlayer != null) {
                mNextPlayer.release();
                mNextPlayer = null;
            }
            if (path == null) return;
            mNextPlayer = new MediaPlayer();
            mNextPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
            mNextPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextPlayer, path)) {
                mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
            } else {
                if (mNextPlayer != null) {
                    mNextPlayer.release();
                    mNextPlayer = null;
                }
            }
        }

        public void setVolume(float v) {
            mCurrentPlayer.setVolume(v, v);
        }

        public void start() {
            mCurrentPlayer.start();
        }

        public void stop() {
            /**
             * Resets the MediaPlayer to its uninitialized state. After calling
             * this method, you will have to initialize it again by setting the
             * data source and calling prepare().
             */
            mCurrentPlayer.reset();
            mIsInitialized = false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mp == mCurrentPlayer && mNextPlayer != null) {
                mCurrentPlayer.release();
                mCurrentPlayer = mNextPlayer;
                mNextPlayer = null;
                mHandler.sendEmptyMessage(TRACK_WENT_TO_NEXT);
            } else {
                mService.get().mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mCurrentPlayer.release();
                    mCurrentPlayer = new MediaPlayer();
                    mCurrentPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
                    return true;
            }
            return false;
        }
    }

    public static class MediaItem {
        public boolean isBrowseable;
        public String album;
        public String artist;
        public String path;
        public String title;
        public String duration;
        public Bitmap bitmap;

        @NonNull
        @Override
        public String toString() {
            return title;
        }
    }

    public class NotificationHelper {
        private static final String ACTION_AUDIO_PLAY = "euphoria.psycho.fun.ACTION_AUDIO_PLAY";
        private static final String CHANNEL_ID = "default";
        private static final String CHANNEL_NAME = "Musice Service";
        private static final int MUSIC_SERVICE = 1 << 1;
        private final NotificationManager mNotificationManager;
        private final MusicService mService;
        private RemoteViews mRemoteView;
        private Notification mNotification;

        public NotificationHelper(MusicService service) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mService = service;
        }

        public void buildNotification(String trackName, String artistName, Bitmap album, boolean isPlaying) {
            mRemoteView = new RemoteViews(mService.getPackageName(), R.layout.notification);
            initializeCollapsedLayout(trackName, artistName, album);
            PendingIntent intent = PendingIntent.getActivity(mService, 0, new Intent(ACTION_AUDIO_PLAY).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                mNotificationManager.createNotificationChannel(channel);
            }
            mNotification = new NotificationCompat.Builder(mService, CHANNEL_ID)
                    .setSmallIcon(R.drawable.stat_notify_music)
                    //.setContentIntent(intent)
                    .setContent(mRemoteView)
                    .build();
            initializePlaybackActions(isPlaying);
            mService.startForeground(MUSIC_SERVICE, mNotification);
        }

        private void initializeCollapsedLayout(String trackName, String artistName, Bitmap album) {
            mRemoteView.setTextViewText(R.id.title, trackName);
            mRemoteView.setTextViewText(R.id.description, artistName);
            mRemoteView.setImageViewBitmap(R.id.imageView, album);
        }

        private void initializePlaybackActions(boolean isPlaying) {
            mRemoteView.setOnClickPendingIntent(R.id.previous, retrievePlaybackActions(3));
            mRemoteView.setOnClickPendingIntent(R.id.play, retrievePlaybackActions(1));
            mRemoteView.setOnClickPendingIntent(R.id.next, retrievePlaybackActions(2));
            mRemoteView.setImageViewResource(R.id.play, isPlaying ? R.drawable.btn_playback_pause : R.drawable.btn_playback_play);
        }

        public void killNotification() {
            mService.stopForeground(true);
            mNotification = null;
        }

        private PendingIntent retrievePlaybackActions(int which) {
            Intent action;
            PendingIntent pendingIntent;
            ComponentName serviceName = new ComponentName(mService, MusicService.class);
            switch (which) {
                case 1:
                    action = new Intent(MusicService.ACTION_TOGGLE_PAUSE);
                    action.setComponent(serviceName);
                    pendingIntent = PendingIntent.getService(mService, 1, action, 0);
                    return pendingIntent;
                case 2:
                    action = new Intent(MusicService.ACTION_NEXT);
                    action.setComponent(serviceName);
                    pendingIntent = PendingIntent.getService(mService, 2, action, 0);
                    return pendingIntent;
                case 3:
                    action = new Intent(MusicService.ACTION_PREVIOUS);
                    action.setComponent(serviceName);
                    pendingIntent = PendingIntent.getService(mService, 3, action, 0);
                    return pendingIntent;
                case 4:
                    action = new Intent(MusicService.ACTION_STOP);
                    action.setComponent(serviceName);
                    pendingIntent = PendingIntent.getService(mService, 4, action, 0);
                    return pendingIntent;
            }
            return null;
        }

        public void updatePlayState(boolean isPlaying) {
            if (mNotification == null || mNotificationManager == null) return;
            if (mRemoteView != null) {
                mRemoteView.setImageViewResource(R.id.play, isPlaying ?
                        R.drawable.btn_playback_pause :
                        R.drawable.btn_playback_play);
            }
            mNotificationManager.notify(MUSIC_SERVICE, mNotification);
        }
    }

    private class MusicPlayerHandler extends Handler {
        private final WeakReference<MusicService> mService;

        public MusicPlayerHandler(MusicService service, Looper looper) {
            super(looper);
            mService = new WeakReference<MusicService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mService.get();
            if (service == null) return;
            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .5f;
                    if (mCurrentVolume > .2f) {
                        sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                    mCurrentVolume += .1f;
                    if (mCurrentVolume < 1.0f) {
                        sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    service.mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    break;
                case FOCUS_CHANGED:
                    handleFocusChange(msg.arg1);
                    break;
                case TRACK_WENT_TO_NEXT:
                    mPosition = mNextPosition;
                    service.updateNotification();
                    service.setNextTrack();
                    break;
                case RELEASE_WAKELOCK:
                    service.mWakeLock.release();
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
