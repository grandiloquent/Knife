package euphoria.psycho.knife.download;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.view.MarginLayoutParamsCompat;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.ViewUtils;
import euphoria.psycho.common.widget.ListMenuButton;
import euphoria.psycho.common.widget.ListMenuButton.Item;
import euphoria.psycho.common.widget.MaterialProgressBar;
import euphoria.psycho.common.widget.selection.SelectableItemView;
import euphoria.psycho.knife.R;

public class DownloadItemView extends SelectableItemView implements ListMenuButton.Delegate {
    private final ColorStateList mCheckedIconForegroundColorList;
    private final int mIconBackgroundResId;
    private final ColorStateList mIconForegroundColorList;
    private final int mMargin;
    private final int mMarginSubsection;
    private DownloadInfo mItem;
    private DownloadManager mDownloadManager;
    private ImageButton mPauseResumeButton;
    private int mIconSize;
    private LinearLayout mLayoutContainer;
    private ListMenuButton mMoreButton;
    private MaterialProgressBar mProgressView;
    private TextView mDescriptionCompletedView;
    private TextView mDownloadPercentageView;
    private TextView mDownloadStatusView;
    private TextView mFilenameCompletedView;
    private TextView mFilenameInProgressView;
    private View mCancelButton;
    private View mLayoutCompleted;
    private View mLayoutInProgress;


    public DownloadItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mMargin = context.getResources().getDimensionPixelSize(R.dimen.list_item_default_margin);
        mMarginSubsection =
                context.getResources().getDimensionPixelSize(R.dimen.list_item_subsection_margin);
        mIconSize = getResources().getDimensionPixelSize(R.dimen.list_item_start_icon_width);
        mCheckedIconForegroundColorList = AppCompatResources.getColorStateList(context, R.color.white_mode_tint);
        mIconBackgroundResId = R.drawable.list_item_icon_modern_bg;
        mIconForegroundColorList =
                AppCompatResources.getColorStateList(context, R.color.dark_mode_tint);
    }

    public void displayItem(DownloadInfo item) {
        updateView();
        mItem = item;
        setItem(item);

        MarginLayoutParamsCompat.setMarginStart(
                (MarginLayoutParams) mLayoutContainer.getLayoutParams(), mMargin);

        Context context = mDescriptionCompletedView.getContext();
        mFilenameCompletedView.setText(item.fileName);
        mFilenameInProgressView.setText(item.fileName);

        if (item.isComplete()) {
            showLayout(mLayoutCompleted);

            // To ensure that text views have correct width after recycling, we have to request
            // re-layout.
            mFilenameCompletedView.requestLayout();
        } else {
            showLayout(mLayoutInProgress);
            //mDownloadStatusView.setText(item.getStatusString());

            //Progress progress = item.getDownloadProgress();

            if (item.isPaused()) {
                mPauseResumeButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
                mPauseResumeButton.setContentDescription(
                        getContext().getString(R.string.download_notification_resume_button));
            } else {
                mPauseResumeButton.setImageResource(R.drawable.ic_pause_white_24dp);
                mPauseResumeButton.setContentDescription(
                        getContext().getString(R.string.download_notification_pause_button));
            }
        }

        mMoreButton.setContentDescriptionContext(item.fileName);
        boolean canShowMore = item.isComplete();
        mMoreButton.setVisibility(canShowMore ? View.VISIBLE : View.GONE);
        mMoreButton.setClickable(true);

        setLongClickable(item.isComplete());

    }

    public void setDownloadManager(DownloadManager downloadManager) {
        mDownloadManager = downloadManager;
    }

    private void showLayout(View layoutToShow) {
        if (mLayoutCompleted != layoutToShow) ViewUtils.removeViewFromParent(mLayoutCompleted);
        if (mLayoutInProgress != layoutToShow) ViewUtils.removeViewFromParent(mLayoutInProgress);

        if (layoutToShow.getParent() == null) {
            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
            params.weight = 1;
            mLayoutContainer.addView(layoutToShow, params);

            // Move the menu button to the back of mLayoutContainer.
            mLayoutContainer.removeView(mMoreButton);
            mLayoutContainer.addView(mMoreButton);
        }
    }

    @Override
    public Item[] getItems() {
        return new Item[]{new Item(getContext(), R.string.share, true),
                new Item(getContext(), R.string.delete, true)};
    }

    @Override
    protected void onClick() {

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mProgressView = findViewById(R.id.download_progress_view);

        mLayoutContainer = findViewById(R.id.layout_container);
        mLayoutCompleted = findViewById(R.id.completed_layout);
        mLayoutInProgress = findViewById(R.id.progress_layout);

        mFilenameCompletedView = findViewById(R.id.filename_completed_view);
        mDescriptionCompletedView = findViewById(R.id.description_view);
        mMoreButton = findViewById(R.id.more);

        mFilenameInProgressView = findViewById(R.id.filename_progress_view);
        mDownloadStatusView = findViewById(R.id.status_view);
        mDownloadPercentageView = findViewById(R.id.percentage_view);

        mPauseResumeButton = findViewById(R.id.pause_button);
        mCancelButton = findViewById(R.id.cancel_button);

        mMoreButton.setDelegate(this);
        mPauseResumeButton.setOnClickListener(view -> {
            if (mItem.isPaused()) {
                mDownloadManager.resume(mItem);
            } else if (!mItem.isComplete()) {
                mDownloadManager.pause(mItem);
            }
        });
        mCancelButton.setOnClickListener(view -> {
            mDownloadManager.cancel(mItem);
        });
    }

    @Override
    public void onItemSelected(Item item) {

    }

    @Override
    protected void updateView() {
        if (isChecked()) {
            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_selected));
            mIconView.setImageDrawable(mCheckDrawable);
            ApiCompatibilityUtils.setImageTintList(mIconView, mCheckedIconForegroundColorList);
            mCheckDrawable.start();
        } else {

            Log.e("TAG/", "updateView: ");

            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_default));
            mIconView.setImageResource(R.drawable.ic_drive_document_24dp);
            ApiCompatibilityUtils.setImageTintList(mIconView, mIconForegroundColorList);
        }
    }
}
