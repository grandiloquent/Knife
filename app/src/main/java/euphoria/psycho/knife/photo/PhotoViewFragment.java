package euphoria.psycho.knife.photo;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import euphoria.psycho.common.Log;
import euphoria.psycho.share.util.ThreadUtils;
import euphoria.psycho.knife.R;

import static euphoria.psycho.knife.photo.PhotoManager.EXTRA_MAX_INITIAL_SCALE;

public class PhotoViewFragment extends Fragment implements
        OnClickListener,
        ImageLoaderObserver,
        OnScreenListener {


    public static final String ARG_PHOTO_ID = "photo_id";
    private static final String ARG_INTENT = "intent";
    private static final String ARG_POSITION = "position";
    private static final String ARG_SHOW_SPINNER = "show_spinner";
    private static final String STATE_INTENT_KEY = "intent_key";
    private boolean mFullScreen;
    private Intent mIntent;
    private PhotoView mPhotoView;
    private CharSequence mContentDescription;
    private TextView mEmptyText;
    private PhotoDelegate mDelegate;
    private int mPosition;
    private PhotoPagerAdapter mAdapter;
    private boolean mOnlyShowSpinner;
    private String mPhotoUri;
    private String mPhotoId;
    private boolean mIsPaused;

    private void bindData(Drawable drawable) {
        if (drawable != null) {
            if (mPhotoView != null) mPhotoView.bindDrawable(drawable);
        }
        enableImageTransforms(true);

    }

    private void displayPhoto(Drawable drawable) {
        if (drawable == null) {
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


        mEmptyText = view.findViewById(R.id.empty_text);

    }

    private boolean isPhotoBound() {
        return mPhotoView != null && mPhotoView.isPhotoBound();
    }


    private void setViewVisibility() {
        mFullScreen = true;
    }

    private static void initializeArguments(Intent intent,
                                            String id,
                                            int position,
                                            boolean onlyShowSpinner,
                                            PhotoViewFragment fragment) {
        final Bundle bundle = new Bundle();
        bundle.putParcelable(ARG_INTENT, intent);
        bundle.putString(ARG_PHOTO_ID, id);
        bundle.putInt(ARG_POSITION, position);
        bundle.putBoolean(ARG_SHOW_SPINNER, onlyShowSpinner);
        fragment.setArguments(bundle);
    }

    static PhotoViewFragment newInstance(Intent intent,
                                         String id,
                                         int position,
                                         boolean onlyShowSpinner) {
        final PhotoViewFragment fragment = new PhotoViewFragment();
        initializeArguments(intent, id, position, onlyShowSpinner, fragment);
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
        mDelegate.toggleFullScreen();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments == null) return;
        mIntent = arguments.getParcelable(ARG_INTENT);

        mPosition = arguments.getInt(ARG_POSITION);
        mPhotoId = arguments.getString(ARG_PHOTO_ID);
        mOnlyShowSpinner = arguments.getBoolean(ARG_SHOW_SPINNER);
        if (savedInstanceState != null) {
            final Bundle state = savedInstanceState.getBundle(STATE_INTENT_KEY);
            if (state != null) {
                mIntent = new Intent().putExtras(state);
            }
        }
        if (mIntent != null) {
            mPhotoUri = mIntent.getStringExtra(PhotoManager.EXTRA_PHOTO_URI);

            Log.e("TAG/PhotoViewFragment", "onCreate: " + mIntent.getExtras());

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
    public void onDetach() {
        mDelegate = null;

        super.onDetach();
    }

    @Override
    public void onFullScreenChanged(boolean fullScreen) {
        setViewVisibility();
    }

    @Override
    public boolean onInterceptMoveLeft(float origX, float origY) {

        if (!mDelegate.isFragmentActive(this)) {
            return false;
        }
        return (mPhotoView != null && mPhotoView.interceptMoveLeft(origX, origY));
    }

    private void loadImage() {
        mDelegate.getImageLoader().loadImage(mPhotoUri, mPhotoId, this);

    }

    @Override
    public boolean onInterceptMoveRight(float origX, float origY) {

        if (!mDelegate.isFragmentActive(this)) {
            return false;
        }
        return (mPhotoView != null && mPhotoView.interceptMoveRight(origX, origY));
    }

    @Override
    public void onLoadFinished(Drawable drawable) {
        if (getView() == null || !isAdded()) return;
        ThreadUtils.postOnUiThread(() -> {

            displayPhoto(drawable);

            if (drawable != null) {
                mDelegate.onNewPhotoLoaded(mPosition);
            }
            setViewVisibility();
            mDelegate.getImageLoader().removeJob(mPhotoId);

        });
    }

    @Override
    public void onPause() {
        mDelegate.removeScreenListener(mPosition);
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
        mDelegate.addScreenListener(mPosition, this);
        if (!isPhotoBound()) {
            loadImage();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mIntent != null) {
            outState.putParcelable(STATE_INTENT_KEY, mIntent.getExtras());
        }
    }

    @Override
    public void onViewActivated() {
        if (!mDelegate.isFragmentActive(this)) {
            resetViews();
        } else {
            if (!isPhotoBound()) {
                loadImage();
            }
            mDelegate.onFragmentVisible(this);
        }
    }

    public void resetViews() {
        if (mPhotoView != null) {
            mPhotoView.resetTransformations();
        }
    }

    @Override
    public void onViewUpNext() {
        resetViews();
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
