package euphoria.common;

import android.app.Activity;
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
}
