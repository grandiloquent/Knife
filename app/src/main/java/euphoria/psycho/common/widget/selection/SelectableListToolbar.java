package euphoria.psycho.common.widget.selection;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.share.util.ColorUtils;
import euphoria.psycho.common.annotations.VisibleForTesting;
import euphoria.psycho.common.widget.KeyboardVisibilityDelegate;
import euphoria.psycho.common.widget.NumberRollView;
import euphoria.psycho.common.widget.TintedDrawable;
import euphoria.psycho.common.widget.selection.SelectionDelegate.SelectionObserver;
import euphoria.psycho.knife.R;


public class SelectableListToolbar<E>
        extends Toolbar implements SelectionObserver<E>, OnClickListener, OnEditorActionListener {
    /**
     * A delegate that handles searching the list of selectable items associated with this toolbar.
     */
    public interface SearchDelegate {
        /**
         * Called when the text in the search EditText box has changed.
         *
         * @param query The text in the search EditText box.
         */
        void onSearchTextChanged(String query);

        /**
         * Called when a search is ended.
         */
        void onEndSearch();
    }

    /**
     * No navigation button is displayed.
     **/
    public static final int NAVIGATION_BUTTON_NONE = 0;
    /**
     * Button to open the DrawerLayout. Only valid if mDrawerLayout is set.
     **/
    public static final int NAVIGATION_BUTTON_MENU = 1;
    /**
     * Button to navigate back. This calls {@link #onNavigationBack()}.
     **/
    public static final int NAVIGATION_BUTTON_BACK = 2;
    /**
     * Button to clear the selection.
     **/
    public static final int NAVIGATION_BUTTON_SELECTION_BACK = 3;

    protected boolean mIsSelectionEnabled;
    protected SelectionDelegate<E> mSelectionDelegate;

    private boolean mIsSearching;
    private boolean mHasSearchView;
    private LinearLayout mSearchView;
    private EditText mSearchText;
    private EditText mSearchEditText;
    private ImageButton mClearTextButton;
    private SearchDelegate mSearchDelegate;
    private boolean mSearchEnabled;
    private boolean mIsVrEnabled;
    private boolean mUpdateStatusBarColor;

    protected NumberRollView mNumberRollView;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private TintedDrawable mNormalMenuButton;
    private TintedDrawable mSelectionMenuButton;
    private TintedDrawable mNavigationIconDrawable;

    private int mNavigationButton;
    private int mTitleResId;
    private int mSearchMenuItemId;
    private int mInfoMenuItemId;
    private int mNormalGroupResId;
    private int mSelectedGroupResId;

    private int mNormalBackgroundColor;
    private int mSelectionBackgroundColor;
    private int mSearchBackgroundColor;
    private ColorStateList mDarkIconColorList;
    private ColorStateList mLightIconColorList;

    private int mWideDisplayStartOffsetPx;
    private int mModernNavButtonStartOffsetPx;
    private int mModernToolbarActionMenuEndOffsetPx;
    private int mModernToolbarSearchIconOffsetPx;

    private boolean mIsDestroyed;
    private boolean mShowInfoItem;
    private boolean mInfoShowing;

    private boolean mShowInfoIcon;
    private int mShowInfoStringId;
    private int mHideInfoStringId;
    private int mExtraMenuItemId;

    /**
     * Constructor for inflating from XML.
     */
    public SelectableListToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Destroys and cleans up itself.
     */
    @CallSuper
    public void destroy() {
        mIsDestroyed = true;
        if (mSelectionDelegate != null) mSelectionDelegate.removeObserver(this);
        KeyboardVisibilityDelegate.getInstance().hideKeyboard(mSearchEditText);
    }

    /**
     * Initializes the SelectionToolbar.
     *
     * @param delegate             The SelectionDelegate that will inform the toolbar of selection changes.
     * @param titleResId           The resource id of the title string. May be 0 if this class shouldn't set
     *                             set a title when the selection is cleared.
     * @param drawerLayout         The DrawerLayout whose navigation icon is displayed in this toolbar.
     * @param normalGroupResId     The resource id of the menu group to show when a selection isn't
     *                             established.
     * @param selectedGroupResId   The resource id of the menu item to show when a selection is
     *                             established.
     * @param updateStatusBarColor Whether the status bar color should be updated to match the
     *                             toolbar color. If true, the status bar will only be updated if
     *                             the current device fully supports theming and is on Android M+.
     */
    public void initialize(SelectionDelegate<E> delegate, int titleResId,
                           @Nullable DrawerLayout drawerLayout, int normalGroupResId, int selectedGroupResId,
                           boolean updateStatusBarColor) {
        mTitleResId = titleResId;
        mDrawerLayout = drawerLayout;
        mNormalGroupResId = normalGroupResId;
        mSelectedGroupResId = selectedGroupResId;
        // TODO(twellington): Setting the status bar color crashes on Nokia devices. Re-enable
        // after a Nokia test device is procured and the crash can be debugged.
        // See https://crbug.com/880694.
        mUpdateStatusBarColor = false;

        mSelectionDelegate = delegate;
        mSelectionDelegate.addObserver(this);

        mModernNavButtonStartOffsetPx = getResources().getDimensionPixelSize(
                R.dimen.selectable_list_toolbar_nav_button_start_offset);
        mModernToolbarActionMenuEndOffsetPx = getResources().getDimensionPixelSize(
                R.dimen.selectable_list_action_bar_end_padding);
        mModernToolbarSearchIconOffsetPx = getResources().getDimensionPixelSize(
                R.dimen.selectable_list_search_icon_end_padding);

        if (mDrawerLayout != null) initActionBarDrawerToggle();

        mNormalBackgroundColor =
                ApiCompatibilityUtils.getColor(getResources(), R.color.modern_primary_color);
        setBackgroundColor(mNormalBackgroundColor);

        mSelectionBackgroundColor = ApiCompatibilityUtils.getColor(
                getResources(), R.color.light_active_color);

        mDarkIconColorList =
                AppCompatResources.getColorStateList(getContext(), R.color.dark_mode_tint);
        mLightIconColorList =
                AppCompatResources.getColorStateList(getContext(), R.color.white_mode_tint);

        setTitleTextAppearance(getContext(), R.style.BlackHeadline);
        if (mTitleResId != 0) setTitle(mTitleResId);

        // TODO(twellington): add the concept of normal & selected tint to apply to all toolbar
        //                    buttons.
        mNormalMenuButton = TintedDrawable.constructTintedDrawable(
                getContext(), R.drawable.ic_more_vert_black_24dp);
        mSelectionMenuButton = TintedDrawable.constructTintedDrawable(
                getContext(), R.drawable.ic_more_vert_black_24dp, R.color.white_mode_tint);
        mNavigationIconDrawable = TintedDrawable.constructTintedDrawable(
                getContext(), R.drawable.ic_arrow_back_white_24dp);


        mShowInfoIcon = true;
        mShowInfoStringId = R.string.show_info;
        mHideInfoStringId = R.string.hide_info;

        // Used only for the case of DownloadManagerToolbar.
        // Will not be needed after a tint is applied to all toolbar buttons.
        MenuItem extraMenuItem = getMenu().findItem(mExtraMenuItemId);
        if (extraMenuItem != null) {
            Drawable iconDrawable = TintedDrawable.constructTintedDrawable(
                    getContext(), R.drawable.ic_more_vert_black_24dp, R.color.dark_mode_tint);
            extraMenuItem.setIcon(iconDrawable);
        }
    }


    /**
     * Inflates and initializes the search view.
     *
     * @param searchDelegate   The delegate that will handle performing searches.
     * @param hintStringResId  The hint text to show in the search view's EditText box.
     * @param searchMenuItemId The menu item used to activate the search view. This item will be
     *                         hidden when selection is enabled or if the list of selectable items
     *                         associated with this toolbar is empty.
     */
    public void initializeSearchView(SearchDelegate searchDelegate, int hintStringResId,
                                     int searchMenuItemId) {
        mHasSearchView = true;
        mSearchDelegate = searchDelegate;
        mSearchMenuItemId = searchMenuItemId;
        mSearchBackgroundColor = Color.WHITE;

        LayoutInflater.from(getContext()).inflate(R.layout.search_toolbar, this);

        mSearchView = (LinearLayout) findViewById(R.id.search_view);
        mSearchText = (EditText) mSearchView.findViewById(R.id.search_text);

        mSearchEditText = (EditText) findViewById(R.id.search_text);
        mSearchEditText.setHint(hintStringResId);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mClearTextButton.setVisibility(
                        TextUtils.isEmpty(s) ? View.INVISIBLE : View.VISIBLE);
                if (mIsSearching) mSearchDelegate.onSearchTextChanged(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mClearTextButton = findViewById(R.id.clear_text_button);
        mClearTextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchEditText.setText("");
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.number_roll_view, this);
        mNumberRollView = (NumberRollView) findViewById(R.id.selection_mode_number);
        mNumberRollView.setString(R.plurals.selected_items);
        mNumberRollView.setStringForZero(R.string.select_items);
    }

    @Override
    @CallSuper
    public void onSelectionStateChange(List<E> selectedItems) {
        boolean wasSelectionEnabled = mIsSelectionEnabled;
        mIsSelectionEnabled = mSelectionDelegate.isSelectionEnabled();

        // If onSelectionStateChange() gets called before onFinishInflate(), mNumberRollView
        // will be uninitialized. See crbug.com/637948.
        if (mNumberRollView == null) {
            mNumberRollView = (NumberRollView) findViewById(R.id.selection_mode_number);
        }

        if (mIsSelectionEnabled) {
            showSelectionView(selectedItems, wasSelectionEnabled);
        } else if (mIsSearching) {
            showSearchViewInternal();
        } else {
            showNormalView();
        }

        if (mIsSelectionEnabled) {
            @StringRes
            int resId = wasSelectionEnabled ? R.string.accessibility_toolbar_multi_select
                    : R.string.accessibility_toolbar_screen_position;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                announceForAccessibility(
                        getContext().getString(resId, Integer.toString(selectedItems.size())));
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (mIsDestroyed) return;

        switch (mNavigationButton) {
            case NAVIGATION_BUTTON_NONE:
                break;
            case NAVIGATION_BUTTON_MENU:
                // ActionBarDrawerToggle handles this.
                break;
            case NAVIGATION_BUTTON_BACK:
                onNavigationBack();
                break;
            case NAVIGATION_BUTTON_SELECTION_BACK:
                mSelectionDelegate.clearSelection();
                break;
            default:
                assert false : "Incorrect navigation button state";
        }
    }

    /**
     * Handle a click on the navigation back button. If this toolbar has a search view, the search
     * view will be hidden. Subclasses should override this method if navigation back is also a
     * valid toolbar action when not searching.
     */
    public void onNavigationBack() {
        if (!mHasSearchView || !mIsSearching) return;

        hideSearchView();
    }

    /**
     * Update the current navigation button (the top-left icon on LTR)
     *
     * @param navigationButton one of NAVIGATION_BUTTON_* constants.
     */
    protected void setNavigationButton(int navigationButton) {
        int contentDescriptionId = 0;

        if (navigationButton == NAVIGATION_BUTTON_MENU && mDrawerLayout == null) {
            mNavigationButton = NAVIGATION_BUTTON_NONE;
        } else {
            mNavigationButton = navigationButton;
        }

        if (mNavigationButton == NAVIGATION_BUTTON_MENU) {
            initActionBarDrawerToggle();
            // ActionBarDrawerToggle will take care of icon and content description, so just return.
            return;
        }

        if (mActionBarDrawerToggle != null) {
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerLayout.removeDrawerListener(mActionBarDrawerToggle);
            mActionBarDrawerToggle = null;
        }

        setNavigationOnClickListener(this);

        switch (mNavigationButton) {
            case NAVIGATION_BUTTON_NONE:
                break;
            case NAVIGATION_BUTTON_BACK:
                mNavigationIconDrawable.setTint(mDarkIconColorList);
                contentDescriptionId = R.string.accessibility_toolbar_btn_back;
                break;
            case NAVIGATION_BUTTON_SELECTION_BACK:
                mNavigationIconDrawable.setTint(mLightIconColorList);
                contentDescriptionId = R.string.accessibility_cancel_selection;
                break;
            default:
                assert false : "Incorrect navigationButton argument";
        }

        setNavigationIcon(contentDescriptionId == 0 ? null : mNavigationIconDrawable);
        setNavigationContentDescription(contentDescriptionId);

    }

    /**
     * Shows the search edit text box and related views.
     */
    public void showSearchView() {
        assert mHasSearchView;

        mIsSearching = true;
        mSelectionDelegate.clearSelection();

        showSearchViewInternal();

        mSearchEditText.requestFocus();
        KeyboardVisibilityDelegate.getInstance().showKeyboard(mSearchEditText);
        setTitle(null);
    }

//    /**
//     * Set a custom delegate for when the action mode starts showing for the search view.
//     *
//     * @param delegate The delegate to use.
//     */
//    public void setActionBarDelegate(ActionModeController.ActionBarDelegate delegate) {
//        ToolbarActionModeCallback callback = new ToolbarActionModeCallback();
//        callback.setActionModeController(new ActionModeController(getContext(), delegate));
//        mSearchText.setCustomSelectionActionModeCallback(callback);
//    }

    /**
     * Hides the search edit text box and related views.
     */
    public void hideSearchView() {
        assert mHasSearchView;

        if (!mIsSearching) return;

        mIsSearching = false;
        mSearchEditText.setText("");
        KeyboardVisibilityDelegate.getInstance().hideKeyboard(mSearchEditText);
        showNormalView();

        mSearchDelegate.onEndSearch();
    }

    /**
     * Called to enable/disable search menu button.
     *
     * @param searchEnabled Whether the search button should be enabled.
     */
    public void setSearchEnabled(boolean searchEnabled) {
        if (mHasSearchView) {
            mSearchEnabled = searchEnabled;
            updateSearchMenuItem();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            KeyboardVisibilityDelegate.getInstance().hideKeyboard(v);
        }
        return false;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mIsDestroyed) return;

        mSelectionDelegate.clearSelection();
        if (mIsSearching) hideSearchView();
        if (mDrawerLayout != null) mDrawerLayout.closeDrawer(GravityCompat.START);
    }


    /**
     * @return Whether search mode is currently active. Once a search is started, this method will
     * return true until the search is ended regardless of whether the toolbar view changes
     * dues to a selection.
     */
    public boolean isSearching() {
        return mIsSearching;
    }

    SelectionDelegate<E> getSelectionDelegate() {
        return mSelectionDelegate;
    }

    /**
     * Set up ActionBarDrawerToggle, a.k.a. hamburger button.
     */
    private void initActionBarDrawerToggle() {
        // Sadly, the only way to set correct toolbar button listener for ActionBarDrawerToggle
        // is constructing, so we will need to construct every time we re-show this button.
        mActionBarDrawerToggle = new ActionBarDrawerToggle((Activity) getContext(),
                mDrawerLayout, this,
                R.string.accessibility_drawer_toggle_btn_open,
                R.string.accessibility_drawer_toggle_btn_close);
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
    }

    protected void showNormalView() {
        getMenu().setGroupVisible(mNormalGroupResId, true);
        getMenu().setGroupVisible(mSelectedGroupResId, false);
        if (mHasSearchView) {
            mSearchView.setVisibility(View.GONE);
            updateSearchMenuItem();
        }

        setNavigationButton(NAVIGATION_BUTTON_MENU);
        setBackgroundColor(mNormalBackgroundColor);
        setOverflowIcon(mNormalMenuButton);
        if (mTitleResId != 0) setTitle(mTitleResId);

        mNumberRollView.setVisibility(View.GONE);
        mNumberRollView.setNumber(0, false);

    }

    protected void showSelectionView(List<E> selectedItems, boolean wasSelectionEnabled) {
        getMenu().setGroupVisible(mNormalGroupResId, false);
        getMenu().setGroupVisible(mSelectedGroupResId, true);
        getMenu().setGroupEnabled(mSelectedGroupResId, !selectedItems.isEmpty());
        if (mHasSearchView) mSearchView.setVisibility(View.GONE);

        setNavigationButton(NAVIGATION_BUTTON_SELECTION_BACK);
        setBackgroundColor(mSelectionBackgroundColor);
        setOverflowIcon(mSelectionMenuButton);

        switchToNumberRollView(selectedItems, wasSelectionEnabled);

        if (mIsSearching) KeyboardVisibilityDelegate.getInstance().hideKeyboard(mSearchEditText);

    }

    private void showSearchViewInternal() {
        getMenu().setGroupVisible(mNormalGroupResId, false);
        getMenu().setGroupVisible(mSelectedGroupResId, false);
        mNumberRollView.setVisibility(View.GONE);
        mSearchView.setVisibility(View.VISIBLE);

        setNavigationButton(NAVIGATION_BUTTON_BACK);
        setBackgroundResource(R.drawable.search_toolbar_modern_bg);
        updateStatusBarColor(mSearchBackgroundColor);

    }

    private void updateSearchMenuItem() {
        if (!mHasSearchView) return;
        MenuItem searchMenuItem = getMenu().findItem(mSearchMenuItemId);
        if (searchMenuItem != null) {
            searchMenuItem.setVisible(
                    mSearchEnabled && !mIsSelectionEnabled && !mIsSearching && !mIsVrEnabled);
        }
    }

    protected void switchToNumberRollView(List<E> selectedItems, boolean wasSelectionEnabled) {
        setTitle(null);
        mNumberRollView.setVisibility(View.VISIBLE);
        if (!wasSelectionEnabled) mNumberRollView.setNumber(0, false);
        mNumberRollView.setNumber(selectedItems.size(), true);
    }


    /**
     * Set info menu item used to toggle info header.
     *
     * @param infoMenuItemId The menu item to show or hide information.
     */
    public void setInfoMenuItem(int infoMenuItemId) {
        mInfoMenuItemId = infoMenuItemId;
    }

    /**
     * Set ID of menu item that is displayed to hold any extra actions.
     * Needs to be called before {@link #initialize}.
     *
     * @param extraMenuItemId The menu item.
     */
    public void setExtraMenuItem(int extraMenuItemId) {
        mExtraMenuItemId = extraMenuItemId;
    }

    /**
     * Sets the parameter that determines whether to show the info icon.
     * This is useful when the info menu option is being shown in a sub-menu, where only the text is
     * necessary, versus being shown as an icon in the toolbar.
     * Needs to be called before {@link #initialize}.
     *
     * @param shouldShow Whether to show the icon for the info menu item. Defaults to true.
     */
    public void setShowInfoIcon(boolean shouldShow) {
        mShowInfoIcon = shouldShow;
    }

    /**
     * Set the IDs of the string resources to be shown for the info button text if different from
     * the default "Show info"/"Hide info" text.
     * Needs to be called before {@link #initialize}.
     *
     * @param showInfoStringId Resource ID of string for the info button text that, when clicked,
     *                         will show info.
     * @param hideInfoStringId Resource ID of the string that will hide the info.
     */
    public void setInfoButtonText(int showInfoStringId, int hideInfoStringId) {
        mShowInfoStringId = showInfoStringId;
        mHideInfoStringId = hideInfoStringId;
    }

    /**
     * Update icon, title, and visibility of info menu item.
     *
     * @param showItem    Whether or not info menu item should show.
     * @param infoShowing Whether or not info header is currently showing.
     */
    public void updateInfoMenuItem(boolean showItem, boolean infoShowing) {
        mShowInfoItem = showItem;
        mInfoShowing = infoShowing;

        MenuItem infoMenuItem = getMenu().findItem(mInfoMenuItemId);
        if (infoMenuItem != null) {
            if (mShowInfoIcon) {
                Drawable iconDrawable =
                        TintedDrawable.constructTintedDrawable(getContext(), R.drawable.btn_info,
                                infoShowing ? R.color.blue_mode_tint : R.color.dark_mode_tint);

                infoMenuItem.setIcon(iconDrawable);
            }

            infoMenuItem.setTitle(infoShowing ? mHideInfoStringId : mShowInfoStringId);
            infoMenuItem.setVisible(showItem);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        // The super class adds an AppCompatTextView for the title which not focusable by default.
        // Set TextView children to focusable so the title can gain focus in accessibility mode.
        makeTextViewChildrenAccessible();
    }

    @Override
    public void setBackgroundColor(int color) {
        super.setBackgroundColor(color);

        updateStatusBarColor(color);
    }

    private void updateStatusBarColor(int color) {
        if (!mUpdateStatusBarColor) return;

        Context context = getContext();
        if (!(context instanceof Activity)) return;

        Window window = ((Activity) context).getWindow();
        ApiCompatibilityUtils.setStatusBarColor(window, color);
        ApiCompatibilityUtils.setStatusBarIconColor(window.getDecorView().getRootView(),
                !ColorUtils.shouldUseLightForegroundOnBackground(color));
    }

    @VisibleForTesting
    public View getSearchViewForTests() {
        return mSearchView;
    }

    @VisibleForTesting
    public int getNavigationButtonForTests() {
        return mNavigationButton;
    }

    /**
     * Ends any in-progress animations.
     */
    @VisibleForTesting
    public void endAnimationsForTesting() {
        mNumberRollView.endAnimationsForTesting();
    }

    private void makeTextViewChildrenAccessible() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child instanceof TextView)) continue;
            child.setFocusable(true);

            // setFocusableInTouchMode is problematic for buttons, see
            // https://crbug.com/813422.
            if ((child instanceof Button)) continue;
            child.setFocusableInTouchMode(true);
        }
    }
}
