package euphoria.video;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public interface PlayerFragmentDelegate {
    void pause();

    int getCurrentVideoPosition();

    void videoPlaybackStopped();

    void onError(String message);

    void onCompletion();

    IjkMediaPlayer getPlayer();

    void onPrepared();

}