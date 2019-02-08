package euphoria.psycho.common;

public class ViewUtils {

    private static float sDensity = 0f;


    public static int dp2px(int dp) {
        if (sDensity == 0f) {
            sDensity = ContextUtils.getApplicationContext().getResources().getDisplayMetrics().density;
        }


        float f = dp * sDensity;
        return (int) ((f >= 0) ? (f + 0.5f) : (f - 0.5f));
    }
}
