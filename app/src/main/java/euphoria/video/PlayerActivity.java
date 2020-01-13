package euphoria.video;

import androidx.activity.ComponentActivity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import euphoria.psycho.knife.R;
import euphoria.video.PlayerFragment.PlayerDelegate;

public class PlayerActivity extends AppCompatActivity implements PlayerDelegate {
    private static final String PREF_KEY_SCREEN_ORIENTATION = "pref_key_screen_orientation";
    private static final String PREF_SCREEN_AUTO_VALUE = "pref_screen_auto_value";
    private static final String PREF_SCREEN_LANDSCAPE_VALUE = "pref_screen_landscape_value";
    private static final String PREF_SCREEN_PORTRAIT_VALUE = "pref_screen_portrait_value";
    private static final String PREF_SCREEN_SENSOR_VALUE = "pref_screen_sensor_value";
    PlayerFragmentDelegate mPlayerFragmentDelegate;
    private WakeLock mWakeLock;

    @Override
    public void onBackPressed() {
        if (mPlayerFragmentDelegate != null) {
            mPlayerFragmentDelegate.videoPlaybackStopped();
        }
        super.onBackPressed();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_main);
        FragmentEx fragment = new PlayerFragment();
        mPlayerFragmentDelegate = (PlayerFragmentDelegate) fragment;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mPlayerFragmentDelegate != null) mPlayerFragmentDelegate.videoPlaybackStopped();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String str = PreferenceManager.getDefaultSharedPreferences(this).getString(
                PREF_KEY_SCREEN_ORIENTATION,
                PREF_SCREEN_AUTO_VALUE);
        int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (str.equals(PREF_SCREEN_LANDSCAPE_VALUE))
            orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        if (str.equals(PREF_SCREEN_PORTRAIT_VALUE))
            orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
        if (str.equals(PREF_SCREEN_SENSOR_VALUE))
            orientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        setRequestedOrientation(orientation);

        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .requestAudioFocus(null, AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        super.onStart();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Knife:WAKE_LOCK");
        mWakeLock.acquire();
    }

    @Override
    protected void onStop() {
        ((AudioManager) getSystemService(AUDIO_SERVICE))
                .abandonAudioFocus(null);
        super.onStop();

        mWakeLock.release();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
/*

 */