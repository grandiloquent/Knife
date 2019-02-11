package euphoria.psycho.common;

import android.view.View;
import android.view.ViewGroup;

public class ViewUtils {

    private static float sDensity = 0f;

    public static void removeViewFromParent(View view) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return;
        parent.removeView(view);
    }
    public static int dp2px(int dp) {
        if (sDensity == 0f) {
            sDensity = ContextUtils.getApplicationContext().getResources().getDisplayMetrics().density;
        }


        float f = dp * sDensity;
        return (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
    }
}
