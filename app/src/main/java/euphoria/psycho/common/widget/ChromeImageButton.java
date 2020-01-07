package euphoria.psycho.common.widget;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageButton;
/**
 * A subclass of AppCompatImageButton to add workarounds for bugs in Android Framework and Support
 * Library.
 */
public class ChromeImageButton extends AppCompatImageButton {
    public ChromeImageButton(Context context) {
        super(context);
    }
    public ChromeImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ChromeImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void drawableStateChanged() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            // Pre-N ImageView doesn't correctly invalidate drawables, see https://crbug.com/894770.
            Drawable drawable = getDrawable();
            if (drawable != null && drawable.isStateful()
                    && drawable.setState(getDrawableState())) {
                invalidateDrawable(drawable);
            }
        }
        super.drawableStateChanged();
    }
}
