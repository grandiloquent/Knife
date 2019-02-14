package euphoria.psycho.knife.photo;

import android.net.Uri;
import android.provider.OpenableColumns;

public final class PhotoContract {
    /** Columns for the view */
    public static interface PhotoViewColumns {
        /**
         * This column is a {@link Uri} that can be queried
         * for this individual image (resulting cursor has one single row for this image).
         */
        public static final String URI = "uri";
        /**
         * This column is a {@link String} that can be queried for this
         * individual image to return a displayable name.
         */
        public static final String NAME = OpenableColumns.DISPLAY_NAME;
        /**
         * This column is a {@link Uri} that points to the downloaded local file.
         * Can be null.
         */
        public static final String CONTENT_URI = "contentUri";
        /**
         * This column is a {@link Uri} that points to a thumbnail of the image
         * that ideally is a local file.
         * Can be null.
         */
        public static final String THUMBNAIL_URI = "thumbnailUri";
        /**
         * This string column is the MIME type.
         */
        public static final String CONTENT_TYPE = "contentType";
        /**
         * This boolean column indicates that a loading indicator should display permanently
         * if no image urls are provided.
         */
        public static final String LOADING_INDICATOR = "loadingIndicator";
    }

    public static interface PhotoQuery {
        /** Projection of the returned cursor */
        public final static String[] PROJECTION = {
            PhotoViewColumns.URI,
            PhotoViewColumns.NAME,
            PhotoViewColumns.CONTENT_URI,
            PhotoViewColumns.THUMBNAIL_URI,
            PhotoViewColumns.CONTENT_TYPE
        };

        public final static String[] OPTIONAL_COLUMNS = {
            PhotoViewColumns.LOADING_INDICATOR
        };
    }

    public static final class ContentTypeParameters {
        /**
         * Parameter used to specify which type of content to return.
         * Allows multiple types to be specified.
         */
        public static final String CONTENT_TYPE = "contentType";

        private ContentTypeParameters() {}
    }
}
