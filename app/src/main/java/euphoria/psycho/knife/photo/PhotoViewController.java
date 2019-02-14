package euphoria.psycho.knife.photo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import androidx.fragment.app.FragmentManager;
import androidx.loader.app.LoaderManager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import euphoria.psycho.knife.photo.ActionBarInterface.OnMenuVisibilityListener;
import euphoria.psycho.knife.photo.PhotoViewPager.OnInterceptTouchListener;

public class PhotoViewController implements OnPageChangeListener, OnInterceptTouchListener,
        OnMenuVisibilityListener, PhotoViewCallbacks {
    private final ActivityInterface mActivity;

    public PhotoViewController(ActivityInterface activity) {
        mActivity = activity;

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public boolean onBackPressed() {
    }

    public boolean onCreateOptionsMenu(Menu menu) {
    }

    public void onDestroy() {
    }

    public boolean onOptionsItemSelected(MenuItem item) {
    }

    public void onPause() {
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
    }

    public void onResume() {
    }

    public void onSaveInstanceState(Bundle outState) {
    }

    public void onStart() {
    }

    public void onStop() {
    }

    public interface ActivityInterface {
        public <T extends View> T findViewById(int id);

        public void finish();

        public ActionBarInterface getActionBarInterface();

        public Context getApplicationContext();

        public Context getContext();

        public PhotoViewController getController();

        public Intent getIntent();

        public Resources getResources();

        public FragmentManager getSupportFragmentManager();

        public LoaderManager getSupportLoaderManager();

        public boolean onOptionsItemSelected(MenuItem item);

        public void overridePendingTransition(int enterAnim, int exitAnim);

        public void setContentView(int resId);
    }

    public void onCreate(Bundle savedInstanceState) {
    }
}
