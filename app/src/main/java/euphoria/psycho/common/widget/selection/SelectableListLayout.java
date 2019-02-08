package euphoria.psycho.common.widget.selection;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver;
import androidx.recyclerview.widget.RecyclerView.ItemAnimator;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.annotations.VisibleForTesting;
import euphoria.psycho.common.widget.FadingShadow;
import euphoria.psycho.common.widget.FadingShadowView;
import euphoria.psycho.common.widget.LoadingView;
import euphoria.psycho.common.widget.selection.SelectionDelegate.SelectionObserver;
import euphoria.psycho.knife.R;


/**
 * Contains UI elements common to selectable list views: a loading view, empty view, selection
 * toolbar, shadow, and RecyclerView.
 * <p>
 * After the SelectableListLayout is inflated, it should be initialized through calls to
 * #initializeRecyclerView(), #initializeToolbar(), and #initializeEmptyView().
 *
 * @param <E> The type of the selectable items this layout holds.
 */
public class SelectableListLayout<E>
        extends FrameLayout implements SelectionObserver<E> {

    private static final int WIDE_DISPLAY_MIN_PADDING_DP = 16;
    private RecyclerView.Adapter mAdapter;
    private ViewStub mToolbarStub;
    private TextView mEmptyView;
    private LoadingView mLoadingView;
    private RecyclerView mRecyclerView;
    private ItemAnimator mItemAnimator;
    SelectableListToolbar<E> mToolbar;
    private FadingShadowView mToolbarShadow;
    boolean mShowShadowOnSelection;

    private int mEmptyStringResId;
    private int mSearchEmptyStringResId;


    private final AdapterDataObserver mAdapterObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            Log.d("TAG/", "initializeRecyclerViewProperties: ");

            if (mAdapter.getItemCount() == 0) {
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mEmptyView.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
            // At inflation, the RecyclerView is set to gone, and the loading view is visible. As
            // long as the adapter data changes, we show the recycler view, and hide loading view.
            mLoadingView.hideLoadingUI();

            mToolbar.setSearchEnabled(mAdapter.getItemCount() != 0);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            updateEmptyViewVisibility();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            updateEmptyViewVisibility();
        }
    };

    public SelectableListLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.selectable_list_layout, this);

        mEmptyView = (TextView) findViewById(R.id.empty_view);
        mLoadingView = (LoadingView) findViewById(R.id.loading_view);
        mLoadingView.showLoadingUI();

        mToolbarStub = (ViewStub) findViewById(R.id.action_bar_stub);

        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    /**
     * Creates a RecyclerView for the given adapter.
     *
     * @param adapter The adapter that provides a binding from an app-specific data set to views
     *                that are displayed within the RecyclerView.
     * @return The RecyclerView itself.
     */
    public RecyclerView initializeRecyclerView(RecyclerView.Adapter adapter) {
        return initializeRecyclerView(adapter, null);
    }

    /**
     * Initializes the layout with the given recycler view and adapter.
     *
     * @param adapter      The adapter that provides a binding from an app-specific data set to views
     *                     that are displayed within the RecyclerView.
     * @param recyclerView The recycler view to be shown.
     * @return The RecyclerView itself.
     */
    public RecyclerView initializeRecyclerView(
            RecyclerView.Adapter adapter, @Nullable RecyclerView recyclerView) {
        mAdapter = adapter;

        if (recyclerView == null) {
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        } else {
            mRecyclerView = recyclerView;

            // Replace the inflated recycler view with the one supplied to this method.
            FrameLayout contentView = (FrameLayout) findViewById(R.id.list_content);
            RecyclerView existingView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
            contentView.removeView(existingView);
            contentView.addView(mRecyclerView, 0);
        }

        mRecyclerView.setAdapter(mAdapter);
        initializeRecyclerViewProperties();
        return mRecyclerView;
    }

    private void initializeRecyclerViewProperties() {
        mAdapter.registerAdapterDataObserver(mAdapterObserver);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                setToolbarShadowVisibility();
            }
        });

        mItemAnimator = mRecyclerView.getItemAnimator();
    }

    /**
     * Initializes the SelectionToolbar.
     *
     * @param toolbarLayoutId       The resource id of the toolbar layout. This will be inflated into
     *                              a ViewStub.
     * @param delegate              The SelectionDelegate that will inform the toolbar of selection changes.
     * @param titleResId            The resource id of the title string. May be 0 if this class shouldn't set
     *                              set a title when the selection is cleared.
     * @param drawerLayout          The DrawerLayout whose navigation icon is displayed in this toolbar.
     * @param normalGroupResId      The resource id of the menu group to show when a selection isn't
     *                              established.
     * @param selectedGroupResId    The resource id of the menu item to show when a selection is
     *                              established.
     * @param listener              The OnMenuItemClickListener to set on the toolbar.
     * @param showShadowOnSelection Whether to show the toolbar shadow on selection.
     * @param updateStatusBarColor  Whether the status bar color should be updated to match the
     *                              toolbar color. If true, the status bar will only be updated if
     *                              the current device fully supports theming and is on Android M+.
     * @return The initialized SelectionToolbar.
     */
    public SelectableListToolbar<E> initializeToolbar(int toolbarLayoutId,
                                                      SelectionDelegate<E> delegate, int titleResId, @Nullable DrawerLayout drawerLayout,
                                                      int normalGroupResId, int selectedGroupResId,
                                                      @Nullable Toolbar.OnMenuItemClickListener listener, boolean showShadowOnSelection,
                                                      boolean updateStatusBarColor) {
        mToolbarStub.setLayoutResource(toolbarLayoutId);
        @SuppressWarnings("unchecked")
        SelectableListToolbar<E> toolbar = (SelectableListToolbar<E>) mToolbarStub.inflate();
        mToolbar = toolbar;
        mToolbar.initialize(delegate, titleResId, drawerLayout, normalGroupResId,
                selectedGroupResId, updateStatusBarColor);

        if (listener != null) {
            mToolbar.setOnMenuItemClickListener(listener);
        }

        mToolbarShadow = (FadingShadowView) findViewById(R.id.shadow);
        mToolbarShadow.init(
                ApiCompatibilityUtils.getColor(getResources(), R.color.toolbar_shadow_color),
                FadingShadow.POSITION_TOP);

        mShowShadowOnSelection = showShadowOnSelection;
        delegate.addObserver(this);
        setToolbarShadowVisibility();

        return mToolbar;
    }

    /**
     * Initializes the view shown when the selectable list is empty.
     *
     * @param emptyDrawable          The Drawable to show when the selectable list is empty.
     * @param emptyStringResId       The string to show when the selectable list is empty.
     * @param searchEmptyStringResId The string to show when the selectable list is empty during
     *                               a search.
     * @return The {@link TextView} displayed when the list is empty.
     */
    public TextView initializeEmptyView(
            Drawable emptyDrawable, int emptyStringResId, int searchEmptyStringResId) {
        mEmptyStringResId = emptyStringResId;
        mSearchEmptyStringResId = searchEmptyStringResId;

        mEmptyView.setCompoundDrawablesWithIntrinsicBounds(null, emptyDrawable, null, null);
        mEmptyView.setText(mEmptyStringResId);

        return mEmptyView;
    }

    /**
     * Called when the view that owns the SelectableListLayout is destroyed.
     */
    public void onDestroyed() {
        mAdapter.unregisterAdapterDataObserver(mAdapterObserver);
        mToolbar.getSelectionDelegate().removeObserver(this);
        mToolbar.destroy();
        mRecyclerView.setAdapter(null);
    }


    @Override
    public void onSelectionStateChange(List<E> selectedItems) {
        setToolbarShadowVisibility();
    }

    /**
     * Called when a search is starting.
     */
    public void onStartSearch() {
        mRecyclerView.setItemAnimator(null);
        mToolbarShadow.setVisibility(View.VISIBLE);
        mEmptyView.setText(mSearchEmptyStringResId);
    }

    /**
     * Called when a search has ended.
     */
    public void onEndSearch() {
        mRecyclerView.setItemAnimator(mItemAnimator);
        setToolbarShadowVisibility();
        mEmptyView.setText(mEmptyStringResId);
    }


    private void setToolbarShadowVisibility() {
        if (mToolbar == null || mRecyclerView == null) return;

        boolean showShadow = mRecyclerView.canScrollVertically(-1)
                || (mToolbar.getSelectionDelegate().isSelectionEnabled() && mShowShadowOnSelection);
        mToolbarShadow.setVisibility(showShadow ? View.VISIBLE : View.GONE);
    }

    /**
     * Unlike ListView or GridView, RecyclerView does not provide default empty
     * view implementation. We need to check it ourselves.
     */
    private void updateEmptyViewVisibility() {
        mEmptyView.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @VisibleForTesting
    public View getToolbarShadowForTests() {
        return mToolbarShadow;
    }

    /**
     * Called when the user presses the back key. Note that this method is not called automatically.
     * The embedding UI must call this method
     * when a backpress is detected for the event to be handled.
     *
     * @return Whether this event is handled.
     */
    public boolean onBackPressed() {
        SelectionDelegate selectionDelegate = mToolbar.getSelectionDelegate();
        if (selectionDelegate.isSelectionEnabled()) {
            selectionDelegate.clearSelection();
            return true;
        }

        if (mToolbar.isSearching()) {
            mToolbar.hideSearchView();
            return true;
        }

        return false;
    }
}