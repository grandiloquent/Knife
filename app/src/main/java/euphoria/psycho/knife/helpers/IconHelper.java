package euphoria.psycho.knife.helpers;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import euphoria.psycho.common.C;
import euphoria.psycho.knife.R;
import euphoria.psycho.share.util.ContextUtils;

public class IconHelper {

    private static SparseArray<Drawable> mIcons = new SparseArray<>();

    public static Drawable getIcon(int type) {

        Drawable drawable = mIcons.get(type);
        if (drawable == null) {
            Resources resources = ContextUtils.getApplicationContext().getResources();
            switch (type) {
                case C.TYPE_DIRECTORY:
                    drawable = resources.getDrawable(R.drawable.ic_type_folder);
                    break;
                case C.TYPE_AUDIO:
                    drawable = resources.getDrawable(R.drawable.ic_type_music);
                    break;
                case C.TYPE_VIDEO:
                    drawable = resources.getDrawable(R.drawable.ic_type_video);
                    break;
                case C.TYPE_APK:
                    drawable = resources.getDrawable(R.drawable.ic_type_apk);
                    break;
                case C.TYPE_TEXT:
                    drawable = resources.getDrawable(R.drawable.ic_type_text);
                    break;
                case C.TYPE_PDF:
                    drawable = resources.getDrawable(R.drawable.ic_type_pdf);
                    break;
                case C.TYPE_ZIP:
                    drawable = resources.getDrawable(R.drawable.ic_type_zip);
                    break;
                default:
                    drawable = resources.getDrawable(R.drawable.ic_type_others);
                    break;
            }
            mIcons.put(type, drawable);
            return drawable;
        }
        return drawable;
    }


}
