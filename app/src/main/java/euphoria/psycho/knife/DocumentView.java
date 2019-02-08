package euphoria.psycho.knife;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;

import androidx.appcompat.content.res.AppCompatResources;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.C;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.widget.ListMenuButton;
import euphoria.psycho.common.widget.selection.SelectableItemView;
import euphoria.psycho.common.widget.selection.SelectionDelegate;

public class DocumentView extends SelectableItemView<DocumentInfo> implements ListMenuButton.Delegate {

    private final ColorStateList mCheckedIconForegroundColorList;
    private final int mIconBackgroundResId;
    private final ColorStateList mIconForegroundColorList;
    DocumentActionDelegate mDelegate;
    DocumentInfo mDocumentInfo;
    private ListMenuButton mMore;

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIconBackgroundResId = R.drawable.list_item_icon_modern_bg;
        mCheckedIconForegroundColorList = DocumentUtils.getIconForegroundColorList(context);
        mIconForegroundColorList =
                AppCompatResources.getColorStateList(context, R.color.dark_mode_tint);
    }


    @Override
    public void setItem(DocumentInfo documentInfo) {
        if (getItem() == documentInfo) return;
        super.setItem(documentInfo);
        mDocumentInfo = documentInfo;

        mTitleView.setText(documentInfo.getFileName());
        setIconDrawable(IconHelper.getIcon(documentInfo.getType()));
        if (documentInfo.getType() == C.TYPE_DIRECTORY) {
            mDescriptionView.setText(getContext().getString(R.string.directory_description, documentInfo.getSize()));
        } else {
            mDescriptionView.setText(FileUtils.formatFileSize(documentInfo.getSize()));
        }
    }

    public void initializeActionDelegate(DocumentActionDelegate delegate, SelectionDelegate selectionDelegate) {
        mDelegate = delegate;
        setSelectionDelegate(selectionDelegate);
    }

    @Override
    public ListMenuButton.Item[] getItems() {
        Context context = getContext();
        if (mDocumentInfo.getType() == C.TYPE_VIDEO) {
            return new ListMenuButton.Item[]{
                    new ListMenuButton.Item(getContext(), R.string.share, true),
                    new ListMenuButton.Item(getContext(), R.string.delete, true),
                    new ListMenuButton.Item(getContext(), R.string.trim_video, true),
                    new ListMenuButton.Item(context, R.string.properties, true)
            };
        }
        return new ListMenuButton.Item[]{
                new ListMenuButton.Item(getContext(), R.string.share, true),
                new ListMenuButton.Item(getContext(), R.string.delete, true),
                new ListMenuButton.Item(context, R.string.properties, true)

        };
    }

    @Override
    protected void onClick() {
        mDelegate.onClicked(mDocumentInfo);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMore = findViewById(R.id.more);
        mMore.setDelegate(this);

    }

    @Override
    public void onItemSelected(ListMenuButton.Item item) {
        switch (item.getTextId()) {
            case R.string.share:
                mDelegate.share(mDocumentInfo);
                break;
            case R.string.delete:
                mDelegate.delete(mDocumentInfo);
                break;
            case R.string.trim_video:
                mDelegate.trimVideo(mDocumentInfo);
                break;
            case R.string.properties:
                mDelegate.getProperties(mDocumentInfo);
                break;
        }
    }

    @Override
    public void setSelectionDelegate(SelectionDelegate<DocumentInfo> delegate) {
        super.setSelectionDelegate(delegate);
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
            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_default));
            mIconView.setImageDrawable(IconHelper.getIcon(mDocumentInfo.getType()));

        }
    }
}
