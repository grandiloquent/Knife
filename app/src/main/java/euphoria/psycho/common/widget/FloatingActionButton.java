package euphoria.psycho.common.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.ShapeDrawable.ShaderFactory;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import euphoria.psycho.knife.R;

public class FloatingActionButton extends View {

    int mColorDisabled;
    private boolean mStrokeVisible;
    private Drawable mIconDrawable;
    private final int mColorPressed;
    private float mCircleSize;
    private float mShadowRadius;
    private float mIconSize;
    private float mShadowOffset;
    private float mStrokeWidth;
    private int mDrawableSize;
    private int mSize;
    private int mColorNormal;
    private int mIcon;
    public static final int SIZE_NORMAL = 0;
    public static final int SIZE_MINI = 1;


    public FloatingActionButton(Context context, AttributeSet attrs) {

        super(context, attrs);
        setClickable(true);
        Resources res = getResources();
        mColorNormal = res.getColor(android.R.color.holo_blue_dark);
        mColorDisabled = res.getColor(android.R.color.darker_gray);
        mColorPressed = res.getColor(android.R.color.holo_blue_light);
        mStrokeVisible = true;
        mIcon = 0;
        mSize = SIZE_MINI;

        updateCircleSize();
        mShadowRadius = res.getDisplayMetrics().density * 9;
        mStrokeWidth = res.getDisplayMetrics().density * 1;
        mIconSize = res.getDisplayMetrics().density * 24;
        mShadowOffset = res.getDisplayMetrics().density * 3;
        updateDrawableSize();

        updateBackground();
        // Log.e("TAG/", "\n res = " + res + "\n mColorNormal = " + mColorNormal + "\n mColorDisabled = " + mColorDisabled + "\n mColorPressed = " + mColorPressed + "\n mStrokeVisible = " + mStrokeVisible + "\n mIcon = " + mIcon + "\n mShadowRadius = " + mShadowRadius + "\n mStrokeWidth = " + mStrokeWidth + "\n mIconSize = " + mIconSize + "\n mShadowOffset = " + mShadowOffset);

    }

    private Drawable createCircleDrawable(int color, float strokeWidth) {
        int alpha = Color.alpha(color);
        int opaqueColor = opaque(color);

        ShapeDrawable fillDrawable = new ShapeDrawable(new OvalShape());

        final Paint paint = fillDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setColor(opaqueColor);

        Drawable[] layers = {
                fillDrawable,
                createInnerStrokesDrawable(opaqueColor, strokeWidth)
        };

        LayerDrawable drawable = alpha == 255 || !mStrokeVisible
                ? new LayerDrawable(layers)
                : new TranslucentLayerDrawable(alpha, layers);

        int halfStrokeWidth = (int) (strokeWidth / 2f);
        drawable.setLayerInset(1, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth, halfStrokeWidth);

        return drawable;
    }

    private static class TranslucentLayerDrawable extends LayerDrawable {
        private final int mAlpha;

        public TranslucentLayerDrawable(int alpha, Drawable... layers) {
            super(layers);
            mAlpha = alpha;
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();
            canvas.saveLayerAlpha(bounds.left, bounds.top, bounds.right, bounds.bottom, mAlpha, Canvas.ALL_SAVE_FLAG);
            super.draw(canvas);
            canvas.restore();
        }
    }

    private int darkenColor(int argb) {
        return adjustColorBrightness(argb, 0.9f);
    }

    private int lightenColor(int argb) {
        return adjustColorBrightness(argb, 1.1f);
    }

    private int adjustColorBrightness(int argb, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(argb, hsv);

        hsv[2] = Math.min(hsv[2] * factor, 1f);

        return Color.HSVToColor(Color.alpha(argb), hsv);
    }

    private int halfTransparent(int argb) {
        return Color.argb(
                Color.alpha(argb) / 2,
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    public void setIconDrawable(Drawable iconDrawable) {
        if (mIconDrawable != iconDrawable) {
            mIcon = 0;
            mIconDrawable = iconDrawable;
            updateBackground();
        }
    }

    private Drawable createInnerStrokesDrawable(final int color, float strokeWidth) {
        if (!mStrokeVisible) {
            return new ColorDrawable(Color.TRANSPARENT);
        }

        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        final int bottomStrokeColor = darkenColor(color);
        final int bottomStrokeColorHalfTransparent = halfTransparent(bottomStrokeColor);
        final int topStrokeColor = lightenColor(color);
        final int topStrokeColorHalfTransparent = halfTransparent(topStrokeColor);

        final Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
        shapeDrawable.setShaderFactory(new ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                return new LinearGradient(width / 2, 0, width / 2, height,
                        new int[]{topStrokeColor, topStrokeColorHalfTransparent, color, bottomStrokeColorHalfTransparent, bottomStrokeColor},
                        new float[]{0f, 0.2f, 0.5f, 0.8f, 1f},
                        TileMode.CLAMP
                );
            }
        });

        return shapeDrawable;
    }

    private StateListDrawable createFillDrawable(float strokeWidth) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-android.R.attr.state_enabled}, createCircleDrawable(mColorDisabled, strokeWidth));
        drawable.addState(new int[]{android.R.attr.state_pressed}, createCircleDrawable(mColorPressed, strokeWidth));
        drawable.addState(new int[]{}, createCircleDrawable(mColorNormal, strokeWidth));
        return drawable;
    }

    private int opaque(int argb) {
        return Color.rgb(
                Color.red(argb),
                Color.green(argb),
                Color.blue(argb)
        );
    }

    Drawable getIconDrawable() {
        if (mIconDrawable != null) {
            return mIconDrawable;
        } else if (mIcon != 0) {
            return getResources().getDrawable(mIcon);
        } else {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    }

    private int opacityToAlpha(float opacity) {
        return (int) (255f * opacity);
    }

    private Drawable createOuterStrokeDrawable(float strokeWidth) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());

        final Paint paint = shapeDrawable.getPaint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setAlpha(opacityToAlpha(0.02f));

        return shapeDrawable;
    }

    void updateBackground() {
        final float strokeWidth = mStrokeWidth;
        final float halfStrokeWidth = strokeWidth / 2f;

        LayerDrawable layerDrawable = new LayerDrawable(
                new Drawable[]{
                        getResources().getDrawable(mSize == SIZE_NORMAL ? R.drawable.fab_bg_normal : R.drawable.fab_bg_mini),
                        createFillDrawable(strokeWidth),
                        createOuterStrokeDrawable(strokeWidth),
                        getIconDrawable()
                });

        int iconOffset = (int) (mCircleSize - mIconSize) / 2;

        int circleInsetHorizontal = (int) (mShadowRadius);
        int circleInsetTop = (int) (mShadowRadius - mShadowOffset);
        int circleInsetBottom = (int) (mShadowRadius + mShadowOffset);

        layerDrawable.setLayerInset(1,
                circleInsetHorizontal,
                circleInsetTop,
                circleInsetHorizontal,
                circleInsetBottom);

        layerDrawable.setLayerInset(2,
                (int) (circleInsetHorizontal - halfStrokeWidth),
                (int) (circleInsetTop - halfStrokeWidth),
                (int) (circleInsetHorizontal - halfStrokeWidth),
                (int) (circleInsetBottom - halfStrokeWidth));

        layerDrawable.setLayerInset(3,
                circleInsetHorizontal + iconOffset,
                circleInsetTop + iconOffset,
                circleInsetHorizontal + iconOffset,
                circleInsetBottom + iconOffset);

        setBackgroundCompat(layerDrawable);
    }

    private void setBackgroundCompat(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    private void updateDrawableSize() {
        mDrawableSize = (int) (mCircleSize + 2 * mShadowRadius);
    }

    private void updateCircleSize() {
        if (mSize == SIZE_NORMAL) {
            mCircleSize = getResources().getDisplayMetrics().density * 56;
        } else {
            mCircleSize = getResources().getDisplayMetrics().density * 40;

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mDrawableSize, mDrawableSize);
    }
}
