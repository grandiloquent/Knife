package euphoria.psycho.knife;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import java.io.ByteArrayOutputStream;

import androidx.appcompat.content.res.AppCompatResources;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.BitmapUtils;
import euphoria.psycho.common.C;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.IconUtils;
import euphoria.psycho.common.Log;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.pool.BytesBufferPool;
import euphoria.psycho.common.widget.ListMenuButton;
import euphoria.psycho.common.widget.selection.SelectableItemView;
import euphoria.psycho.common.widget.selection.SelectionDelegate;

import static euphoria.psycho.common.C.DEBUG;

public class DocumentView extends SelectableItemView<DocumentInfo> implements ListMenuButton.Delegate {

    private static final String TAG = "TAG/" + DocumentView.class.getSimpleName();
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

    public void initializeActionDelegate(DocumentActionDelegate delegate, SelectionDelegate selectionDelegate) {
        mDelegate = delegate;
        setSelectionDelegate(selectionDelegate);
    }

    private void updateImageView(DocumentInfo documentInfo, boolean isVideo) {
        ThreadUtils.postOnBackgroundThread(() -> {
            String path = documentInfo.getPath();
            Drawable drawable = null;
            BytesBufferPool.BytesBuffer bytesBuffer = App.getBytesBufferPool().get();

            boolean result = App.getImageCacheService().getImageData(documentInfo.getPath(),
                    C.THUMBNAIL_SMALL, bytesBuffer);

            if (result) {

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytesBuffer.data, bytesBuffer.offset, bytesBuffer.length);

                drawable = new BitmapDrawable(getResources(), bitmap);

            } else {
                Bitmap bitmap;
                if (isVideo) {

                    bitmap = BitmapUtils.createVideoThumbnail(mDocumentInfo.getPath());

                    if (bitmap != null) {
                        bitmap = BitmapUtils.resizeAndCropCenter(bitmap, C.THUMBNAIL_SMALL_SIZE, true);
                        drawable = new BitmapDrawable(getResources(), bitmap);
                    }

                } else {
                    drawable = IconUtils.getAppIcon(documentInfo.getPath());
                    bitmap = IconUtils.drawableToBitmap(drawable);
                    if (bitmap != null)
                        bitmap = BitmapUtils.resizeAndCropCenter(bitmap, C.THUMBNAIL_SMALL_SIZE, true);

                }

                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(65536);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                    App.getImageCacheService().putImageData(documentInfo.getPath(),

                            C.THUMBNAIL_SMALL, baos.toByteArray());
                }
            }

            Drawable finalDrawable = drawable;
            ThreadUtils.postOnUiThread(() ->
            {
                if (finalDrawable != null) {
                    MemoryCache.instance().put(documentInfo.getPath(), finalDrawable);
                    if (path.equals(mDocumentInfo.getPath()))
                        setIconDrawable(finalDrawable);
                    //mIconView.setImageDrawable(finalDrawable);
                }
            });
        });
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
    public void setItem(DocumentInfo documentInfo) {
        if (getItem() == documentInfo) return;
        super.setItem(documentInfo);
        mDocumentInfo = documentInfo;

        mTitleView.setText(documentInfo.getFileName());
        setIconDrawable(IconHelper.getIcon(documentInfo.getType()));

        switch (documentInfo.getType()) {
            case C.TYPE_DIRECTORY:
                mDescriptionView.setText(getContext().getString(R.string.directory_description, documentInfo.getSize()));
                break;
            case C.TYPE_APK:
                updateImageView(documentInfo, false);
                break;
            case C.TYPE_VIDEO:
                updateImageView(documentInfo, true);
                break;

        }
        if (documentInfo.getType() != C.TYPE_DIRECTORY) {
            mDescriptionView.setText(FileUtils.formatFileSize(documentInfo.getSize()));
        }
    }

    @Override
    public void setSelectionDelegate(SelectionDelegate<DocumentInfo> delegate) {
        super.setSelectionDelegate(delegate);
    }
//
//    @Override
//    protected void updateView() {
//
//        if (isChecked()) {
//
//
//            mIconView.setBackgroundResource(mIconBackgroundResId);
//            mIconView.getBackground().setLevel(
//                    getResources().getInteger(R.integer.list_item_level_selected));
//            mIconView.setImageDrawable(mCheckDrawable);
//            ApiCompatibilityUtils.setImageTintList(mIconView, mCheckedIconForegroundColorList);
//            mCheckDrawable.start();
//
//        } else {
//
//            mIconView.setBackgroundResource(mIconBackgroundResId);
//
//            mIconView.getBackground().setLevel(
//                    getResources().getInteger(R.integer.list_item_level_default));
//
//
//            Log.e("TAG/", "updateView: " + (getIconDrawable() == null));
//
//            mIconView.setImageDrawable(getIconDrawable());
//
//        }
//    }
}
