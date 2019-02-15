package euphoria.psycho.knife.photo;

public interface PhotoDelegate {
    PhotoPagerAdapter getAdapter();

    void addScreenListener(int position, OnScreenListener listener);


    ImageLoader getImageLoader();


    void toggleFullScreen();

    PhotoManager getPhotoManager();


    void onNewPhotoLoaded(int position);
}
