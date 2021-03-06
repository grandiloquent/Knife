package euphoria.common;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public class Views {

    public static void onClicks(OnClickListener onClickListener, Activity parent, int... resIds) {
        for (int i = 0, j = resIds.length; i < j; i++) {
            View view = parent.findViewById(resIds[i]);
            if (view != null) {
                view.setOnClickListener(onClickListener);
            }
        }
    }


    public static int dp2px(Context context, int v) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (v * density + 0.5f);
    }
}
