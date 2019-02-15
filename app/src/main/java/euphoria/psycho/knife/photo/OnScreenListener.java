package euphoria.psycho.knife.photo;

public interface OnScreenListener {

    /**
     * The full screen state has changed.
     */
    public void onFullScreenChanged(boolean fullScreen);

    /**
     * A new view has been activated and the previous view de-activated.
     */
    public void onViewActivated();

    /**
     * This view is a candidate for being the next view.
     * <p>
     * This will be called when the view is focused completely on the view immediately before
     * or after this one, so that this view can reset itself if nessecary.
     */
    public void onViewUpNext();

    /**
     * Called when a right-to-left touch move intercept is about to occur.
     *
     * @param origX the raw x coordinate of the initial touch
     * @param origY the raw y coordinate of the initial touch
     * @return {@code true} if the touch should be intercepted.
     */
    public boolean onInterceptMoveLeft(float origX, float origY);

    /**
     * Called when a left-to-right touch move intercept is about to occur.
     *
     * @param origX the raw x coordinate of the initial touch
     * @param origY the raw y coordinate of the initial touch
     * @return {@code true} if the touch should be intercepted.
     */
    public boolean onInterceptMoveRight(float origX, float origY);
}