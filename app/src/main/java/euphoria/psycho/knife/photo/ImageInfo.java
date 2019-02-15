package euphoria.psycho.knife.photo;

public class ImageInfo {
    private final String mPath;
    private final String mTitle;
    private final String mId;

    public String getPath() {
        return mPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getId() {
        return mId;
    }

    public ImageInfo(Builder builder) {
        this.mPath = builder.mPath;
        this.mTitle = builder.mTitle;
        this.mId = builder.mId;
    }

    public static class Builder {
        private String mPath;
        private String mTitle;
        private String mId;

        public Builder() {
        }

        public Builder setPath(String path) {
            this.mPath = path;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }

        public Builder setId(String id) {
            this.mId = id;
            return this;
        }

        public ImageInfo build() {
            return new ImageInfo(this);
        }
    }

    public static Builder fromImageInfo(final ImageInfo imageInfo) {
        Builder builder = new Builder();
        builder
                .setPath(imageInfo.getPath())
                .setTitle(imageInfo.getTitle())
                .setId(imageInfo.getId())
        ;
        return builder;
    }
}
