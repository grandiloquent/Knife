package euphoria.psycho.knife.photo;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import euphoria.psycho.share.util.AccessibilityUtils;
import euphoria.psycho.share.util.SystemUtils;
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

    public static final int ALBUM_COUNT_UNKNOWN = -1;
    private final Map<Integer, OnScreenListener>
            mScreenListeners = new HashMap<Integer, OnScreenListener>();
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
    private int mCurrentPhotoIndex = -1;
    String mActionBarTitle;
    String mActionBarSubtitle;
    private int mLastAnnouncedTitle;
    private String mCurrentPhotoUri;
    int mAlbumCount = ALBUM_COUNT_UNKNOWN;
    boolean mIsEmpty;
    boolean mIsTimerLightsOutEnabled;
    long mEnterFullScreenDelayTime;
    private final Runnable mEnterFullScreenRunnable = () -> setFullScreen(true, true);

    private void cancelEnterFullScreenRunnable() {
        mHandler.removeCallbacks(mEnterFullScreenRunnable);
    }

    protected String getPhotoAccessibilityAnnouncement(int position) {
        String announcement = mActionBarTitle;
        if (mActionBarSubtitle != null) {
            announcement = getResources().getString(
                    R.string.titles, mActionBarTitle, mActionBarSubtitle);
        }
        return announcement;
    }

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
            if (imageInfos == null) {
                mIsEmpty = true;
                mAlbumCount = 0;
            } else {
                mIsEmpty = false;
                mAlbumCount = imageInfos.size();
            }
            ThreadUtils.postOnUiThread(() -> {
                mAdapter.switchDatas(imageInfos);
                mViewPager.setVisibility(View.VISIBLE);
                if (mCurrentPhotoIndex < 0)
                    mCurrentPhotoIndex = 0;
                setViewActivated(mCurrentPhotoIndex);
            });
        });
    }

    private void postEnterFullScreenRunnableWithDelay() {
        if (mIsTimerLightsOutEnabled) {
            mHandler.postDelayed(mEnterFullScreenRunnable, mEnterFullScreenDelayTime);
        }
    }

    protected final void setActionBarTitles() {

        getSupportActionBar().setTitle(getInputOrEmpty(mActionBarTitle));
        getSupportActionBar().setSubtitle(getInputOrEmpty(mActionBarSubtitle));
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

    public void setViewActivated(int position) {

        Log.e("TAG/PhotoViewActivity", "setViewActivated: ");

        OnScreenListener listener = mScreenListeners.get(position);
        if (listener != null) {
            listener.onViewActivated();
        }

        mCurrentPhotoIndex = position;
        // FLAG: get the column indexes once in onLoadFinished().
        // That would make this more efficient, instead of looking these up
        // repeatedly whenever we want them.
        mCurrentPhotoUri = mAdapter.getImageInfo(position).getPath();
        updateActionBar();
        if (getPhotoManager().getAccessibilityManager().isEnabled() && mLastAnnouncedTitle != position) {
            String announcement = getPhotoAccessibilityAnnouncement(position);
            if (announcement != null) {
                AccessibilityUtils.announceForAccessibility(mRootView, getPhotoManager().getAccessibilityManager(), announcement);
                mLastAnnouncedTitle = position;
            }
        }

        // Restart the timer to return to fullscreen.
        cancelEnterFullScreenRunnable();
        postEnterFullScreenRunnableWithDelay();
    }

    private void showActionBar() {
    }

    public void updateActionBar() {
        final int position = mViewPager.getCurrentItem();



        mActionBarTitle = mAdapter.getImageInfo(position).getTitle();

        mActionBarSubtitle = getResources().getString(
                R.string.photo_view_count, position + 1, mAlbumCount);


        setActionBarTitles();
    }

    private static final String getInputOrEmpty(String in) {
        if (in == null) {
            return "";
        }
        return in;
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
            mDirectory = SystemUtils.getDCIMDirectory();


        }

        mEnterFullScreenDelayTime =
                getResources().getInteger(R.integer.reenter_fullscreen_delay_time_in_millis);
        mScaleAnimationEnabled = true;

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
            if (mBackground != null) {
                mBackground.setVisibility(View.VISIBLE);
            }
            mViewPager.setVisibility(View.GONE);
            loadImages();
        }
        //setImmersiveMode(false);
    }

    @Override
    public boolean isFragmentActive(PhotoViewFragment fragment) {
        if (mViewPager == null || mAdapter == null) {
            return false;
        }
        return mViewPager.getCurrentItem() == mAdapter.getItemPosition(fragment);
    }

    @Override
    protected String[] needsPermissions() {
        return new String[0];
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItemUtils.addDeleteMenuItem(menu);
        MenuItemUtils.addShareMenuItem(menu);
        return super.onCreateOptionsMenu(menu);

    }

    public void onEnterAnimationComplete() {
        mEnterAnimationFinished = true;
        mViewPager.setVisibility(View.VISIBLE);
        setImmersiveMode(mFullScreen);
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
        if (positionOffset < 0.0001) {
            OnScreenListener before = mScreenListeners.get(position - 1);
            if (before != null) {
                before.onViewUpNext();
            }
            OnScreenListener after = mScreenListeners.get(position + 1);
            if (after != null) {
                after.onViewUpNext();
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPhotoIndex = position;
        setViewActivated(position);

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
