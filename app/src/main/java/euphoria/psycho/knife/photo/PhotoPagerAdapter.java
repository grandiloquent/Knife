package euphoria.psycho.knife.photo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import androidx.collection.SimpleArrayMap;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import euphoria.psycho.knife.photo.Intents.PhotoViewIntentBuilder;
import euphoria.psycho.knife.photo.PhotoContract.PhotoQuery;
import euphoria.psycho.knife.photo.PhotoContract.PhotoViewColumns;

/**
 * Pager adapter for the photo view
 */
public class PhotoPagerAdapter extends BaseCursorPagerAdapter {
    protected SimpleArrayMap<String, Integer> mColumnIndices =
            new SimpleArrayMap<String, Integer>(PhotoQuery.PROJECTION.length);
    protected final float mMaxScale;
    protected boolean mDisplayThumbsFullScreen;

    public PhotoPagerAdapter(
            Context context, FragmentManager fm, Cursor c,
            float maxScale, boolean thumbsFullScreen) {
        super(context, fm, c);
        mMaxScale = maxScale;
        mDisplayThumbsFullScreen = thumbsFullScreen;
    }

    @Override
    public Fragment getItem(Context context, Cursor cursor, int position) {
        final String photoUri = getPhotoUri(cursor);
        final String thumbnailUri = getThumbnailUri(cursor);
        final String contentDescription = getPhotoName(cursor);
        boolean loading = shouldShowLoadingIndicator(cursor);
        boolean onlyShowSpinner = false;
        if(photoUri == null && loading) {
            onlyShowSpinner = true;
        }

        // create new PhotoViewFragment
        final PhotoViewIntentBuilder builder =
                Intents.newPhotoViewFragmentIntentBuilder(mContext, getPhotoViewFragmentClass());
        builder
            .setResolvedPhotoUri(photoUri)
            .setThumbnailUri(thumbnailUri)
            .setContentDescription(contentDescription)
            .setDisplayThumbsFullScreen(mDisplayThumbsFullScreen)
            .setMaxInitialScale(mMaxScale);

        return createPhotoViewFragment(builder.build(), position, onlyShowSpinner);
    }

    protected Class<? extends PhotoViewFragment> getPhotoViewFragmentClass() {
        return PhotoViewFragment.class;
    }

    protected PhotoViewFragment createPhotoViewFragment(
            Intent intent, int position, boolean onlyShowSpinner) {
        return PhotoViewFragment.newInstance(intent, position, onlyShowSpinner);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        mColumnIndices.clear();

        if (newCursor != null) {
            for(String column : PhotoQuery.PROJECTION) {
                mColumnIndices.put(column, newCursor.getColumnIndexOrThrow(column));
            }

            for(String column : PhotoQuery.OPTIONAL_COLUMNS) {
                int index = newCursor.getColumnIndex(column);
                if (index != -1) {
                    mColumnIndices.put(column, index);
                }
            }
        }

        return super.swapCursor(newCursor);
    }

    public String getPhotoUri(Cursor cursor) {
        return getString(cursor, PhotoViewColumns.CONTENT_URI);
    }

    public String getThumbnailUri(Cursor cursor) {
        return getString(cursor, PhotoViewColumns.THUMBNAIL_URI);
    }

    public String getContentType(Cursor cursor) {
        return getString(cursor, PhotoViewColumns.CONTENT_TYPE);
    }

    public String getPhotoName(Cursor cursor) {
        return getString(cursor, PhotoViewColumns.NAME);
    }

    public boolean shouldShowLoadingIndicator(Cursor cursor) {
        String value = getString(cursor, PhotoViewColumns.LOADING_INDICATOR);
        if (value == null) {
            return false;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    private String getString(Cursor cursor, String column) {
        if (mColumnIndices.containsKey(column)) {
            return cursor.getString(mColumnIndices.get(column));
        } else {
            return null;
        }
    }
}
