package euphoria.psycho.common;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import euphoria.psycho.knife.R;
import euphoria.psycho.knife.util.ContextUtils;

public class ViewUtils {

    public static int DEFAULT_FAVICON_CORNER_RADIUS = -1;
    private static float sDensity = 0f;

    public static RoundedBitmapDrawable createRoundedBitmapDrawable(Bitmap icon, int cornerRadius) {
        Resources resources = ContextUtils.getApplicationContext().getResources();
        if (cornerRadius == DEFAULT_FAVICON_CORNER_RADIUS) {
            cornerRadius = resources.getDimensionPixelSize(R.dimen.default_favicon_corner_radius);
        }
        RoundedBitmapDrawable roundedIcon = RoundedBitmapDrawableFactory.create(resources, icon);
        roundedIcon.setCornerRadius(cornerRadius);
        return roundedIcon;
    }

    public static int dp2px(int dp) {
        if (sDensity == 0f) {
            sDensity = ContextUtils.getApplicationContext().getResources().getDisplayMetrics().density;
        }


        float f = dp * sDensity;
        return (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
    }

    public static void removeViewFromParent(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return;
        parent.removeView(view);
    }
}
