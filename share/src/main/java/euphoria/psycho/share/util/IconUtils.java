package euphoria.psycho.share.util;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

public class IconUtils {

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable getAppIcon(String apkPath) {

        if (apkPath != null) {
            final PackageManager pm = ContextUtils.getApplicationContext().getPackageManager();
            try {
                final PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
                if (packageInfo != null) {
                    packageInfo.applicationInfo.sourceDir = packageInfo.applicationInfo.publicSourceDir = apkPath;
                    // know issue with nine patch image instead of drawable

                    return pm.getApplicationIcon(packageInfo.applicationInfo);
                }
            } catch (Exception e) {
            }
        } else {
        }
        return null;
    }

    public static Bitmap getAppIcon(PackageManager packageManager, String packageName) {

        try {
            Drawable drawable = packageManager.getApplicationIcon(packageName);

            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (drawable instanceof AdaptiveIconDrawable) {
                    return drawableToBitmap(drawable);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}
