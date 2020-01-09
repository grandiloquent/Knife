package euphoria.video;
public interface PlayerFragmentDelegate {
    void pause();
    int getCurrentVideoPosition();
    void videoPlaybackStopped();
}
