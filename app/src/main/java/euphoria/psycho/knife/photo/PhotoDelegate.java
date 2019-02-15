package euphoria.psycho.knife.photo;


public interface PhotoDelegate {
    void addScreenListener(int position, OnScreenListener listener);

    PhotoPagerAdapter getAdapter();

    ImageLoader getImageLoader();

    PhotoManager getPhotoManager();

    boolean isFragmentActive(PhotoViewFragment fragment);

    void onFragmentVisible(PhotoViewFragment fragment);

    void onNewPhotoLoaded(int position);

    void removeScreenListener(int position);

    void toggleFullScreen();
}
