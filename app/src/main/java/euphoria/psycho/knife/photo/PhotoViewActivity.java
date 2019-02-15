package euphoria.psycho.knife.photo;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.view.Menu;
import android.view.View;

import java.io.File;
import java.util.List;

import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import euphoria.psycho.share.util.ThreadUtils;
import euphoria.psycho.common.base.BaseActivity;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.photo.PhotoViewPager.InterceptType;
import euphoria.psycho.knife.photo.PhotoViewPager.OnInterceptTouchListener;
import euphoria.psycho.share.util.MenuItemUtils;

public class PhotoViewActivity extends BaseActivity implements
        OnPageChangeListener,
        OnInterceptTouchListener,
        PhotoDelegate {

    private PhotoManager mPhotoManager;
    private PhotoPagerAdapter mAdapter;
    private boolean mFullScreen;
    private boolean mIsPause;
    private Handler mHandler = new Handler();
    boolean mScaleAnimationEnabled;
    int mLastFlags;
    boolean mEnterAnimationFinished;
    View mBackground;
    View mRootView;
    PhotoViewPager mViewPager;
    private File mDirectory;
    private ImageLoader mImageLoader;
    private String mPhotosUri;

    private View getRootView() {
        return mRootView;
    }

    private void hideActionBar() {

    }

    private void initializeToolbar() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    boolean isEnterAnimationFinished() {
        return mEnterAnimationFinished;
    }

    boolean isScaleAnimationEnabled() {
        return mScaleAnimationEnabled;
    }

    private void loadImages() {
        ThreadUtils.postOnBackgroundThread(() -> {
            List<ImageInfo> imageInfos = PhotoManager.collectImageInfos(mDirectory);
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDatas(imageInfos);
            });
        });
    }

    private void postEnterFullScreenRunnableWithDelay() {

    }

    private void setFullScreen(boolean fullScreen, boolean setDelayedRunnable) {
        if (getPhotoManager().isTouchExplorationEnabled()) {
            fullScreen = false;
            setDelayedRunnable = false;
        }
        boolean fullScreenChanged = (fullScreen != mFullScreen);
        mFullScreen = fullScreen;
        if (mFullScreen) {
            setImmersiveMode(true);
        } else {
            setImmersiveMode(false);
            if (setDelayedRunnable) {

                postEnterFullScreenRunnableWithDelay();
            }
        }

        if (fullScreenChanged) {

        }
    }

    public void setImmersiveMode(boolean enabled) {
        int flags = 0;
        final int version = Build.VERSION.SDK_INT;
        final boolean manuallyUpdateActionBar = version < Build.VERSION_CODES.JELLY_BEAN;
        if (enabled &&
                (!isScaleAnimationEnabled() || isEnterAnimationFinished())) {
            // Turning on immersive mode causes an animation. If the scale animation is enabled and
            // the enter animation isn't yet complete, then an immersive mode animation should not
            // occur, since two concurrent animations are very janky.

            // Disable immersive mode for seconary users to prevent b/12015090 (freezing crash)
            // This is fixed in KK_MR2 but there is no way to differentiate between  KK and KK_MR2.
            if (version > Build.VERSION_CODES.KITKAT ||
                    version == Build.VERSION_CODES.KITKAT && !mPhotoManager.kitkatIsSecondaryUser()) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE;
            } else if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                // Clients that use the scale animation should set the following system UI flags to
                // prevent janky animations on exit when the status bar is hidden:
                //     View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_STABLE
                // As well, client should ensure `android:fitsSystemWindows` is set on the root
                // content view.
                flags = View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;
            } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags = View.SYSTEM_UI_FLAG_LOW_PROFILE;
            } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
                flags = View.STATUS_BAR_HIDDEN;
            }

            if (manuallyUpdateActionBar) {
                hideActionBar();
            }
        } else {
            if (version >= Build.VERSION_CODES.KITKAT) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else if (version >= Build.VERSION_CODES.JELLY_BEAN) {
                flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                flags = View.SYSTEM_UI_FLAG_VISIBLE;
            } else if (version >= Build.VERSION_CODES.HONEYCOMB) {
                flags = View.STATUS_BAR_VISIBLE;
            }

            if (manuallyUpdateActionBar) {
                showActionBar();
            }
        }

        if (version >= Build.VERSION_CODES.HONEYCOMB) {
            mLastFlags = flags;
            getRootView().setSystemUiVisibility(flags);
        }
    }

    private void showActionBar() {
    }

    @Override
    public void addScreenListener(int position, OnScreenListener listener) {

    }

    @Override
    public PhotoPagerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this, getPhotoManager().getMaxPhotoSize());
        }
        return mImageLoader;
    }

    @Override
    public PhotoManager getPhotoManager() {
        if (mPhotoManager == null) {
            mPhotoManager = new PhotoManager(this);
        }
        return mPhotoManager;
    }

    @Override
    protected void initialize() {

        setContentView(R.layout.photo_activity_view);

        Intent intent = getIntent();
        if (intent.hasExtra(PhotoManager.EXTRA_PHOTOS_URI)) {
            mPhotosUri = intent.getStringExtra(PhotoManager.EXTRA_PHOTOS_URI);
            mDirectory = new File(mPhotosUri).getParentFile();
        } else {
            mDirectory = PhotoManager.getCameraDirectory();


        }

        initializeToolbar();
        mRootView = findViewById(R.id.photo_activity_root_view);

        mAdapter = new PhotoPagerAdapter(this, getSupportFragmentManager(), 1);

        mBackground = findViewById(R.id.photo_activity_background);

        mViewPager = findViewById(R.id.photo_view_pager);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setOnInterceptTouchListener(this);
        mViewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.photo_page_margin));

        if (!mScaleAnimationEnabled || mEnterAnimationFinished) {
            loadImages();
            if (mBackground != null) {
                mBackground.setVisibility(View.VISIBLE);
            }
        } else {
            mViewPager.setVisibility(View.GONE);
            loadImages();
        }

    }

    @Override
    public boolean isFragmentActive(PhotoViewFragment fragment) {
        return false;
    }

    @Override
    protected String[] needsPermissions() {
        return new String[0];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItemUtils.addShareMenuItem(menu);

        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public void onFragmentVisible(PhotoViewFragment fragment) {

    }

    @Override
    public void onNewPhotoLoaded(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen(mFullScreen, true);
        mIsPause = false;


    }

    @Override
    public InterceptType onTouchIntercept(float origX, float origY) {
        return null;
    }

    @Override
    public void removeScreenListener(int position) {

    }

    @Override
    public void toggleFullScreen() {
        setFullScreen(!mFullScreen, false);

    }
}
