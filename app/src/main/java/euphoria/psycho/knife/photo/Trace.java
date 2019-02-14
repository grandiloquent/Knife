package euphoria.psycho.knife.photo;

import android.os.Build;

/**
 * Stand-in for {@link android.os.Trace}.
 */
public abstract class Trace {

    /**
     * Begins systrace tracing for a given tag. No-op on unsupported platform versions.
     *
     * @param tag systrace tag to use
     *
     * @see android.os.Trace#beginSection(String)
     */
    public static void beginSection(String tag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            android.os.Trace.beginSection(tag);
        }
    }

    /**
     * Ends systrace tracing for the most recently begun section. No-op on unsupported platform
     * versions.
     *
     * @see android.os.Trace#endSection()
     */
    public static void endSection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            android.os.Trace.endSection();
        }
    }

}
