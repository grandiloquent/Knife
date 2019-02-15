package euphoria.psycho.knife.photo;

public class ImageInfo {
    private final String mPath;
    private final String mTitle;

    @Override
    public String toString() {
        return "ImageInfo{" +
                "mPath='" + mPath + '\'' +
                ", mTitle='" + mTitle + '\'' +
                '}';
    }

    public ImageInfo(Builder builder) {
        this.mPath = builder.mPath;
        this.mTitle = builder.mTitle;
    }

    public String getPath() {
        return mPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public static Builder fromImageInfo(final ImageInfo imageInfo) {
        Builder builder = new Builder();
        builder
                .setPath(imageInfo.getPath())
                .setTitle(imageInfo.getTitle())
        ;
        return builder;
    }

    public static class Builder {
        private String mPath;
        private String mTitle;

        public Builder() {
        }

        public ImageInfo build() {
            return new ImageInfo(this);
        }

        public Builder setPath(String path) {
            this.mPath = path;
            return this;
        }

        public Builder setTitle(String title) {
            this.mTitle = title;
            return this;
        }
    }
}
