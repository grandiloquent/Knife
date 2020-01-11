package euphoria.video;

import android.view.View;
import android.view.View.OnClickListener;

import euphoria.video.TimeBar.OnScrubListener;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;

public class PlayerEventListener implements
        OnPreparedListener,
        OnCompletionListener,
        OnScrubListener,
        OnErrorListener {

    private PlayerFragmentDelegate mFragmentDelegate;

    public boolean isDragging() {
        return mIsDragging;
    }

    private boolean mIsDragging;


    public PlayerEventListener(PlayerFragmentDelegate fragmentDelegate) {
        mFragmentDelegate = fragmentDelegate;
    }


    @Override
    public void onScrubStart(TimeBar timeBar, long position) {
        mIsDragging = true;
    }

    @Override
    public void onScrubMove(TimeBar timeBar, long position) {

    }

    @Override
    public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
        if (mFragmentDelegate != null && mFragmentDelegate.getPlayer() != null)
            mFragmentDelegate.getPlayer().seekTo(position);
        mIsDragging = false;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        if (mFragmentDelegate != null) mFragmentDelegate.onPrepared();

    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
        if (mFragmentDelegate != null) mFragmentDelegate.onError(null);
        return false;
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (mFragmentDelegate != null) mFragmentDelegate.onCompletion();

    }


}
