package euphoria.psycho.knife.photo;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import androidx.loader.content.CursorLoader;

/**
 * Loader for a set of photo IDs.
 */
public class PhotoPagerLoader extends CursorLoader {
    private final Uri mPhotosUri;
    private final String[] mProjection;

    public PhotoPagerLoader(
            Context context, Uri photosUri, String[] projection) {
        super(context);
        mPhotosUri = photosUri;
        mProjection = projection != null ? projection : PhotoContract.PhotoQuery.PROJECTION;
    }

    @Override
    public Cursor loadInBackground() {
        Cursor returnCursor = null;

        final Uri loaderUri = mPhotosUri.buildUpon().appendQueryParameter(
                PhotoContract.ContentTypeParameters.CONTENT_TYPE, "image/").build();
        setUri(loaderUri);
        setProjection(mProjection);
        returnCursor = super.loadInBackground();

        return returnCursor;
    }
}
