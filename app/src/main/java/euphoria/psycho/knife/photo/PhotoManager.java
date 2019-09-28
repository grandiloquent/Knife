package euphoria.psycho.knife.photo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import androidx.core.view.accessibility.AccessibilityRecordCompat;
import euphoria.common.Files;

public class PhotoManager {
    public static final String EXTRA_MAX_INITIAL_SCALE = "max_initial_scale";
    public static final String EXTRA_PHOTOS_URI = "photos_uri";
    public static final String EXTRA_PHOTO_URI = "photo_uri";
    public static final String EXTRA_PHOTO_ID = "photo_id";

    private final static long MIN_NORMAL_CLASS = 32;
    private final static long MIN_SMALL_CLASS = 24;
    private int mMaxPhotoSize;
    private Context mContext;
    private int mMemoryClass;
    private AccessibilityManager mAccessibilityManager;

    public PhotoManager(Context context) {
        mContext = context;

        mAccessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    public AccessibilityManager getAccessibilityManager() {
        return mAccessibilityManager;
    }

    public int getMaxPhotoSize() {

        if (mMaxPhotoSize == 0)
            mMaxPhotoSize = mContext.getResources().getDisplayMetrics().widthPixels *
                    mContext.getResources().getDisplayMetrics().heightPixels;
//        if (mMaxPhotoSize == 0) {
//            final DisplayMetrics metrics = new DisplayMetrics();
//            final WindowManager wm = (WindowManager)
//                    mContext.getSystemService(Context.WINDOW_SERVICE);
//            final ImageSize imageSize;
//            if (Build.VERSION.SDK_INT >= 11) {
//                imageSize = ImageSize.NORMAL;
//            } else {
//                if (getMemoryClass() >= MIN_NORMAL_CLASS) {
//                    // We have plenty of memory; use full sized photos
//                    imageSize = ImageSize.NORMAL;
//                } else if (getMemoryClass() >= MIN_SMALL_CLASS) {
//                    // We have slight less memory; use smaller sized photos
//                    imageSize = ImageSize.SMALL;
//                } else {
//                    // We have little memory; use very small sized photos
//                    imageSize = ImageSize.EXTRA_SMALL;
//                }
//            }
//            wm.getDefaultDisplay().getMetrics(metrics);
//            switch (imageSize) {
//                case EXTRA_SMALL:
//                    // Use a photo that's 80% of the "small" size
//                    mMaxPhotoSize = (Math.min(metrics.heightPixels, metrics.widthPixels) * 800) / 1000;
//                    break;
//                case SMALL:
//                    // Fall through.
//                case NORMAL:
//                    // Fall through.
//                default:
//                    mMaxPhotoSize = Math.min(metrics.heightPixels, metrics.widthPixels);
//                    break;
//            }
//
//        }

        return mMaxPhotoSize;
    }

    public int getMemoryClass() {
        if (mMemoryClass == 0) {
            final ActivityManager mgr = (ActivityManager) mContext.getApplicationContext().
                    getSystemService(Activity.ACTIVITY_SERVICE);
            mMemoryClass = mgr.getMemoryClass();
        }
        return mMemoryClass;
    }

    public boolean isTouchExplorationEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return mAccessibilityManager.isTouchExplorationEnabled();
        } else {
            return false;
        }


    }


    public boolean kitkatIsSecondaryUser() {
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.KITKAT) {
            throw new IllegalStateException("kitkatIsSecondary user is only callable on KitKat");
        }
        return Process.myUid() > 100000;
    }

    /**
     * Make an announcement which is related to some sort of a context change. Also see
     * {@link View#announceForAccessibility}
     *
     * @param view                 The view that triggered the announcement
     * @param accessibilityManager AccessibilityManager instance. If it is null, the method can
     *                             obtain an instance itself.
     * @param text                 The announcement text
     */
    public static void announceForAccessibility(
            final View view, AccessibilityManager accessibilityManager,
            final CharSequence text) {
        // Jelly Bean added support for speaking text verbatim
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.announceForAccessibility(text);
            return;
        }

        final Context context = view.getContext().getApplicationContext();
        if (accessibilityManager == null) {
            accessibilityManager = (AccessibilityManager) context.getSystemService(
                    Context.ACCESSIBILITY_SERVICE);
        }

        if (!accessibilityManager.isEnabled()) {
            return;
        }

        final int eventType = AccessibilityEvent.TYPE_VIEW_FOCUSED;

        // Construct an accessibility event with the minimum recommended
        // attributes. An event without a class name or package may be dropped.
        final AccessibilityEvent event = AccessibilityEvent.obtain(eventType);
        event.getText().add(text);
        event.setEnabled(view.isEnabled());
        event.setClassName(view.getClass().getName());
        event.setPackageName(context.getPackageName());

        // JellyBean MR1 requires a source view to set the window ID.
        final AccessibilityRecordCompat record = AccessibilityEventCompat.asRecord(event);
        record.setSource(view);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            view.getParent().requestSendAccessibilityEvent(view, event);
        } else {
            // Sends the event directly through the accessibility manager.
            accessibilityManager.sendAccessibilityEvent(event);
        }
    }

    @Nullable
    public static List<ImageInfo> collectImageInfos(@NonNull File directory) {
        File[] images = directory.listFiles(pathname -> pathname.isFile() && Files.isSupportedImage(pathname.getName()));

        if (images == null || images.length == 0) return null;

        Arrays.sort(images, (o1, o2) -> {
            long result = o1.lastModified() - o2.lastModified();

            if (result > 0) {
                return 1;
            } else if (result < 0) {
                return -1;
            } else {
                return 0;
            }
        });

        List<ImageInfo> imageInfos = new ArrayList<>();
        for (File image : images) {
            imageInfos.add(new ImageInfo
                    .Builder()
                    .setPath(image.getAbsolutePath())
                    .setTitle(image.getName())
                    .build());
        }
        return imageInfos;
    }


    public static enum ImageSize {
        EXTRA_SMALL,
        SMALL,
        NORMAL,
    }
}
