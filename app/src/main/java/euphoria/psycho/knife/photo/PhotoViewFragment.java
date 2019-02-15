package euphoria.psycho.knife.photo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import euphoria.psycho.common.Log;
import euphoria.psycho.knife.R;

import static euphoria.psycho.knife.photo.PhotoManager.EXTRA_MAX_INITIAL_SCALE;

public class PhotoViewFragment extends Fragment implements OnClickListener, ImageLoaderObserver, OnScreenListener {

    public static final String ARG_INTENT = "intent";
    public static final String ARG_POSITION = "position";
    public static final String ARG_SHOW_SPINNER = "show_spinner";
    private static final String STATE_INTENT_KEY = "";
    private boolean mFullScreen;
    private Intent mIntent;
    private PhotoView mPhotoView;
    private CharSequence mContentDescription;
    private View mPhotoPreviewAndProgress;
    private ImageView mPhotoPreviewImage;
    private TextView mEmptyText;
    private ImageView mRetryButton;
    private PhotoDelegate mDelegate;
    private boolean mProgressBarNeeded;
    private int mPosition;
    private PhotoPagerAdapter mAdapter;
    private boolean mOnlyShowSpinner;
    private String mPhotoUri;

    private void bindData(Drawable drawable) {
        if (drawable != null) {
            if (mPhotoView != null) mPhotoView.bindDrawable(drawable);
        }
        enableImageTransforms(true);
        mPhotoPreviewAndProgress.setVisibility(View.GONE);
        mProgressBarNeeded = false;
    }

    private void displayPhoto(Drawable drawable) {
        if (drawable == null) {
            mProgressBarNeeded = false;
            mEmptyText.setText(R.string.failed);
            mEmptyText.setVisibility(View.VISIBLE);
        } else {
            mEmptyText.setVisibility(View.GONE);

            bindData(drawable);


        }
    }

    private void enableImageTransforms(boolean enable) {
        mPhotoView.enableImageTransforms(enable);
    }

    private void initializeView(View view) {


        mPhotoView = view.findViewById(R.id.photo_view);
        mPhotoView.setMaxInitialScale(mIntent.getFloatExtra(EXTRA_MAX_INITIAL_SCALE, 1.0f));
        mPhotoView.setOnClickListener(this);
        mPhotoView.setFullScreen(mFullScreen, false);
        mPhotoView.enableImageTransforms(false);
        mPhotoView.setContentDescription(mContentDescription);

        mPhotoPreviewAndProgress = view.findViewById(R.id.photo_preview);
        mPhotoPreviewImage = view.findViewById(R.id.photo_preview_image);
        ProgressBar indeterminate = view.findViewById(R.id.indeterminate_progress);
        ProgressBar determinate = view.findViewById(R.id.determinate_progress);

        mEmptyText = view.findViewById(R.id.empty_text);
        mRetryButton = view.findViewById(R.id.retry_button);

    }

    public boolean isPhotoBound() {
        return mPhotoView != null && mPhotoView.isPhotoBound();
    }

    private void setViewVisibility() {
        mFullScreen = true;
    }

    private static void initializeArguments(Intent intent,
                                            int position,
                                            boolean onlyShowSpinner,
                                            PhotoViewFragment fragment) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_INTENT, intent);
        bundle.putInt(ARG_POSITION, position);
        bundle.putBoolean(ARG_SHOW_SPINNER, onlyShowSpinner);
        fragment.setArguments(bundle);
    }

    public static PhotoViewFragment newInstance(Intent intent,
                                                int position,
                                                boolean onlyShowSpinner) {
        final PhotoViewFragment fragment = new PhotoViewFragment();
        initializeArguments(intent, position, onlyShowSpinner, fragment);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mDelegate = (PhotoDelegate) getActivity();
        assert mDelegate != null;
        mAdapter = mDelegate.getAdapter();
        assert mAdapter != null;

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments == null) return;
        mIntent = arguments.getParcelable(ARG_INTENT);

        mPosition = arguments.getInt(ARG_POSITION);
        mOnlyShowSpinner = arguments.getBoolean(ARG_SHOW_SPINNER);
        mProgressBarNeeded = true;
        if (savedInstanceState != null) {
            final Bundle state = savedInstanceState.getBundle(STATE_INTENT_KEY);
            if (state != null) {
                mIntent = new Intent().putExtras(state);
            }
        }
        if (mIntent != null) {
            mPhotoUri = mIntent.getStringExtra(PhotoManager.EXTRA_PHOTO_URI);

            Log.e("TAG/PhotoViewFragment", "onCreate: " + mPhotoUri);

        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.photo_fragment_view, container, false);
        initializeView(view);
        return view;
    }

    @Override
    public void onDestroyView() {
        if (mPhotoView != null) {
            mPhotoView.clear();
            mPhotoView = null;

        }
        super.onDestroyView();
    }

    @Override
    public void onFullScreenChanged(boolean fullScreen) {

    }

    @Override
    public boolean onInterceptMoveLeft(float origX, float origY) {
        return false;
    }

    @Override
    public boolean onInterceptMoveRight(float origX, float origY) {
        return false;
    }

    @Override
    public void onLoadFinished(Drawable drawable) {
        if (getView() == null || !isAdded()) return;

        Log.e("TAG/PhotoViewFragment", "onLoadFinished: ");

        if (drawable != null) {
            mDelegate.onNewPhotoLoaded(mPosition);
        }
        displayPhoto(drawable);
        setViewVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDelegate.addScreenListener(mPosition, this);
        if (!isPhotoBound()) {
            mProgressBarNeeded = true;
            mPhotoPreviewAndProgress.setVisibility(View.VISIBLE);
            mDelegate.getImageLoader().loadImage(mPhotoUri, this);
        }
    }

    @Override
    public void onViewActivated() {

    }

    @Override
    public void onViewUpNext() {

    }

    public static interface HorizontallyScrollable {
        /**
         * Return {@code true} if the component needs to receive right-to-left
         * touch movements.
         *
         * @param origX the raw x coordinate of the initial touch
         * @param origY the raw y coordinate of the initial touch
         */

        public boolean interceptMoveLeft(float origX, float origY);

        /**
         * Return {@code true} if the component needs to receive left-to-right
         * touch movements.
         *
         * @param origX the raw x coordinate of the initial touch
         * @param origY the raw y coordinate of the initial touch
         */
        public boolean interceptMoveRight(float origX, float origY);
    }
}
