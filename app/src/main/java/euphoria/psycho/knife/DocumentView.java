package euphoria.psycho.knife;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import euphoria.psycho.common.ApiCompatibilityUtils;
import euphoria.psycho.common.BitmapUtils;
import euphoria.psycho.common.C;
import euphoria.psycho.common.FileUtils;
import euphoria.psycho.common.IconUtils;
import euphoria.psycho.common.ThreadUtils;
import euphoria.psycho.common.Utils;
import euphoria.psycho.common.pool.BytesBufferPool;
import euphoria.psycho.common.widget.ListMenuButton;
import euphoria.psycho.common.widget.selection.SelectableItemView;
import euphoria.psycho.common.widget.selection.SelectionDelegate;
import euphoria.psycho.knife.cache.ThumbnailProvider;

public class DocumentView extends SelectableItemView<DocumentInfo> implements ListMenuButton.Delegate, ThumbnailProvider.ThumbnailRequest {

    private final ColorStateList mCheckedIconForegroundColorList;
    private final int mIconBackgroundResId;
    DocumentActionDelegate mDelegate;
    private ListMenuButton mMore;
    private int mIconSize;
    private Bitmap mThumbnailBitmap;

    private Drawable mIconDrawable;

    public DocumentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIconSize = getResources().getDimensionPixelSize(R.dimen.list_item_start_icon_width);

        mIconBackgroundResId = R.drawable.list_item_icon_modern_bg;
        mCheckedIconForegroundColorList = DocumentUtils.getIconForegroundColorList(context);

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

                    bitmap = BitmapUtils.createVideoThumbnail(getItem().getPath());

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
                    if (path.equals(getItem().getPath())) {
                        App.getImageCacheService().putImageData(documentInfo.getPath(),

                                C.THUMBNAIL_SMALL, baos.toByteArray());
                    }
                }
            }

            Drawable finalDrawable = drawable;
            ThreadUtils.postOnUiThread(() ->
            {
                if (finalDrawable != null) {
                    MemoryCache.instance().put(documentInfo.getPath(), finalDrawable);
                    if (path.equals(getItem().getPath()))
                        setIconDrawable(finalDrawable);
                    //mIconView.setImageDrawable(finalDrawable);
                }
            });
        });
    }

    @Nullable
    @Override
    public String getContentId() {
        return getItem() == null ? "" : Long.toString(Utils.crc64Long(getItem().getPath()));

    }

    @Nullable
    @Override
    public String getFilePath() {
        return getItem() == null ? null : getItem().getPath();
    }

    @Override
    public int getIconSize() {
        return mIconSize;
    }

    @Override
    public ListMenuButton.Item[] getItems() {
        return DocumentUtils.generateListMenu(getContext(), getItem());
    }

    @Nullable
    @Override
    public String getMimeType() {
        return null;
    }

    @Override
    protected void onClick() {
        mDelegate.onClicked(getItem());

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
                mDelegate.share(getItem());
                break;
            case R.string.delete:
                mDelegate.delete(getItem());
                break;
            case R.string.trim_video:
                mDelegate.trimVideo(getItem());
                break;
            case R.string.properties:
                mDelegate.getProperties(getItem());
                break;
            case R.string.rename:
                mDelegate.rename(getItem());
                break;
            case R.string.extract:
                mDelegate.unzip(getItem());
                break;
        }
    }

    @Override
    public void onThumbnailRetrieved(@NonNull String contentId, @Nullable Bitmap thumbnail) {
        if (TextUtils.equals(getContentId(), contentId) && thumbnail != null
                && thumbnail.getWidth() > 0 && thumbnail.getHeight() > 0) {
            assert !thumbnail.isRecycled();
            mThumbnailBitmap = thumbnail;
            updateView();
        }
    }

    @Override
    public void setItem(DocumentInfo documentInfo) {
        if (getItem() == documentInfo) return;
        super.setItem(documentInfo);
        mDelegate.getThumbnailProvider().cancelRetrieval(this);
        mThumbnailBitmap = null;

        mTitleView.setText(documentInfo.getFileName());
        mIconDrawable = IconHelper.getIcon(documentInfo.getType());

        updateView();
        switch (documentInfo.getType()) {
            case C.TYPE_DIRECTORY:
                mDescriptionView.setText(getContext().getString(R.string.directory_description, documentInfo.getSize()));
                break;
            case C.TYPE_APK:
            case C.TYPE_IMAGE:
            case C.TYPE_VIDEO:
                mDelegate.getThumbnailProvider().getThumbnail(this);
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

    @Override
    protected void updateView() {
        if (isChecked()) {
            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_selected));
            mIconView.setImageDrawable(mCheckDrawable);
            ApiCompatibilityUtils.setImageTintList(mIconView, mCheckedIconForegroundColorList);
            mCheckDrawable.start();
        } else if (mThumbnailBitmap != null) {
            assert !mThumbnailBitmap.isRecycled();
            mIconView.setBackground(null);

            mIconView.setImageDrawable(new BitmapDrawable(mThumbnailBitmap));
//            mIconView.setImageDrawable(ViewUtils.createRoundedBitmapDrawable(
//                    Bitmap.createScaledBitmap(mThumbnailBitmap, mIconSize, mIconSize, false),
//                    getResources().getDimensionPixelSize(
//                            R.dimen.list_item_start_icon_corner_radius)));
            ApiCompatibilityUtils.setImageTintList(mIconView, null);
        } else {


            mIconView.setBackgroundResource(mIconBackgroundResId);
            mIconView.getBackground().setLevel(
                    getResources().getInteger(R.integer.list_item_level_default));
            mIconView.setImageDrawable(mIconDrawable);
            ApiCompatibilityUtils.setImageTintList(mIconView, null);
        }
    }
}
