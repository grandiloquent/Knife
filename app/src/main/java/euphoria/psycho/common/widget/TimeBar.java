package euphoria.psycho.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.util.Formatter;
import java.util.concurrent.CopyOnWriteArraySet;

public class TimeBar extends View {
    public static final int DEFAULT_BAR_HEIGHT_DP = 4;
    public static final int DEFAULT_PLAYED_COLOR = 0xFFFFFFFF;
    public static final int DEFAULT_SCRUBBER_DISABLED_SIZE_DP = 0;
    public static final int DEFAULT_SCRUBBER_DRAGGED_SIZE_DP = 16;
    public static final int DEFAULT_SCRUBBER_ENABLED_SIZE_DP = 12;
    public static final int DEFAULT_TOUCH_TARGET_HEIGHT_DP = 26 * 2;
    public static final long TIME_UNSET = Long.MIN_VALUE + 1;
    private static final int FINE_SCRUB_RATIO = 3;
    private static final int FINE_SCRUB_Y_THRESHOLD_DP = -50;
    private final int mFineScrubYThreshold;
    private final CopyOnWriteArraySet<OnScrubListener> mListeners;
    private int mBarHeight;
    private Rect mBufferedBar;
    private Paint mBufferedPaint;
    private int mBufferedPosition;
    private long mDuration;
    private boolean mIsScrubbing;
    private int mLastCoarseScrubXPosition;
    private int[] mLocationOnScreen;
    private Paint mPlayedPaint;
    private int mPosition;
    private Rect mProgressBar;
    private long mScrubPosition;
    private Rect mScrubberBar;
    private int mScrubberDisabledSize;
    private int mScrubberDraggedSize;
    private int mScrubberEnabledSize;
    private int mScrubberPadding;
    private Paint mScrubberPaint;
    private Rect mSeekBounds;
    private Point mTouchPosition;
    private int mTouchTargetHeight;
    private Paint mUnplayedPaint;

    public TimeBar(Context context, AttributeSet attrs) {
        super(context, attrs);

        initializeRects();
        initializePaints();


        DisplayMetrics metrics = getResources().getDisplayMetrics();

        mFineScrubYThreshold = dpToPx(metrics, FINE_SCRUB_Y_THRESHOLD_DP);
        mTouchTargetHeight = dpToPx(metrics, DEFAULT_TOUCH_TARGET_HEIGHT_DP);
        mScrubberEnabledSize = dpToPx(metrics, DEFAULT_SCRUBBER_ENABLED_SIZE_DP);
        mScrubberDisabledSize = dpToPx(metrics, DEFAULT_SCRUBBER_DISABLED_SIZE_DP);
        mScrubberDraggedSize = dpToPx(metrics, DEFAULT_SCRUBBER_DRAGGED_SIZE_DP);
        mScrubberPadding =
                (Math.max(mScrubberDisabledSize, Math.max(mScrubberEnabledSize, mScrubberDraggedSize)) + 1)
                        / 2;
        mBarHeight = dpToPx(metrics, DEFAULT_BAR_HEIGHT_DP);
        mListeners = new CopyOnWriteArraySet<>();
        mTouchPosition = new Point();
        mLocationOnScreen = new int[2];
    }

    public TimeBar(Context context) {
        this(context, null);
    }

    public void addListener(OnScrubListener listener) {
        mListeners.add(listener);
    }

    private void drawPlayhead(Canvas canvas) {
        if (mDuration <= 0) {
            return;
        }
        int playheadX = constrainValue(mScrubberBar.right, mScrubberBar.left, mProgressBar.right);
        int playheadY = mScrubberBar.centerY();
        int scrubberSize;
        if (mIsScrubbing || isFocused()) {
            scrubberSize = mScrubberDraggedSize;
        } else if (isEnabled()) {
            scrubberSize = mScrubberEnabledSize;
        } else {
            scrubberSize = mScrubberDisabledSize;
        }
        int playheadRadius = scrubberSize / 2;
        canvas.drawCircle(playheadX, playheadY, playheadRadius, mScrubberPaint);
    }

    private void drawTimeBar(Canvas canvas) {
        int progressBarHeight = mProgressBar.height();
        int barTop = mProgressBar.centerY() - progressBarHeight / 2;
        int barBottom = barTop + progressBarHeight;

        if (mDuration <= 0) {
//            Log.e("TAG/",  "\n progressBarHeight = " + progressBarHeight +
//                    "\n barTop = " + barTop +
//                    "\n barBottom = " + barBottom);
            canvas.drawRect(mProgressBar.left, barTop, mProgressBar.right, barBottom, mUnplayedPaint);
        }
        int bufferedLeft = mBufferedBar.left;
        int bufferedRight = mBufferedBar.right;
        int progressLeft = Math.max(Math.max(mProgressBar.left, bufferedRight), mScrubberBar.right);
        if (progressLeft < mProgressBar.right) {
            canvas.drawRect(progressLeft, barTop, mProgressBar.right, barBottom, mUnplayedPaint);
        }
        bufferedLeft = Math.max(bufferedLeft, mScrubberBar.right);
        if (bufferedRight > bufferedLeft) {
            canvas.drawRect(bufferedLeft, barTop, bufferedRight, barBottom, mBufferedPaint);
        }
        if (mScrubberBar.width() > 0) {
            canvas.drawRect(mScrubberBar.left, barTop, mScrubberBar.right, barBottom, mPlayedPaint);
        }
    }

    private CharSequence getProgressText() {
        return null;
    }

    private long getScrubberPosition() {
        if (mProgressBar.width() <= 0 || mDuration == TIME_UNSET) {
            return 0;
        }
//        if (mScrubberBar.width() * mDuration / mProgressBar.width() < 0) {
//
//            Log.e("TAG/", "stopScrubbing: error"
//                    + "\nmScrubberBar.width() = " + mScrubberBar.width()
//                    + "\nmDuration = " + mDuration
//                    + "\nmProgressBar.width() =" + mProgressBar.width());
//
//        }
        return mScrubberBar.width() * mDuration / mProgressBar.width();
    }

    private void initializePaints() {
        mUnplayedPaint = new Paint();
        mBufferedPaint = new Paint();
        mScrubberPaint = new Paint();
        mPlayedPaint = new Paint();
        mPlayedPaint.setColor(DEFAULT_PLAYED_COLOR);
        mUnplayedPaint.setColor(getDefaultUnplayedColor(DEFAULT_PLAYED_COLOR));
        mBufferedPaint.setColor(getDefaultBufferedColor(DEFAULT_PLAYED_COLOR));
        mScrubberPaint.setColor(getDefaultScrubberColor(DEFAULT_PLAYED_COLOR));
    }

    private void initializeRects() {
        mSeekBounds = new Rect();
        mProgressBar = new Rect();
        mBufferedBar = new Rect();
        mScrubberBar = new Rect();
    }

    private boolean isInSeekBar(int x, int y) {
        return mSeekBounds.contains(x, y);
    }

    private void positionScrubber(int x) {


        mScrubberBar.right = constrainValue(x, mProgressBar.left, mProgressBar.right);
    }

    public void removeListener(OnScrubListener listener) {
        mListeners.remove(listener);
    }

    private Point resolveRelativeTouchPosition(MotionEvent motionEvent) {
        getLocationOnScreen(mLocationOnScreen);
        mTouchPosition.set(
                ((int) motionEvent.getRawX()) - mLocationOnScreen[0],
                ((int) motionEvent.getRawY()) - mLocationOnScreen[1]);
        return mTouchPosition;
    }

    public void setBufferedPosition(int bufferedPosition) {
        mBufferedPosition = bufferedPosition;
        update();
    }

    public void setDuration(int duration) {

        // Log.e("TAG/", "setDuration: " + duration);

        mDuration = duration;
        if (mIsScrubbing && duration == TIME_UNSET) {
            stopScrubbing(true);
        }
        update();
    }

    public void setPosition(int position) {
        mPosition = position;
        setContentDescription(getProgressText());
        update();

        // Log.e("TAG/", "setPosition: ");

    }

    private void startScrubbing() {
        mIsScrubbing = true;
        setPressed(true);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        for (OnScrubListener listener : mListeners) {
            listener.onScrubStart(this, getScrubberPosition());
        }
    }

    private void stopScrubbing(boolean b) {
        mIsScrubbing = false;
        setPressed(false);
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(false);
        }
        invalidate();

        for (OnScrubListener listener : mListeners) {
            listener.onScrubStop(this, getScrubberPosition(), b);
        }
    }

    private void update() {
        mBufferedBar.set(mProgressBar);
        mScrubberBar.set(mProgressBar);
        long newScrubberTime = mIsScrubbing ? mScrubPosition : mPosition;
        if (mDuration > 0) {
            int bufferedPixelWidth = (int) (mProgressBar.width() * mBufferedPosition / mDuration);
            mBufferedBar.right = Math.min(mProgressBar.left + bufferedPixelWidth, mProgressBar.right);
            int scrubberPixelPosition = (int) (mProgressBar.width() * newScrubberTime / mDuration);
            mScrubberBar.right = Math.min(mProgressBar.left + scrubberPixelPosition, mProgressBar.right);

//            if (mScrubberBar.right < 0) {
//
//                Log.e("TAG/", "update: "
//                        + "\nscrubberPixelPosition = " + scrubberPixelPosition);
//
//            }

            // Log.e("TAG/", "\n newScrubberTime = " + newScrubberTime + "\n bufferedPixelWidth = " + bufferedPixelWidth + "\n scrubberPixelPosition = " + scrubberPixelPosition);

        } else {
            mBufferedBar.right = mProgressBar.left;
            mScrubberBar.right = mProgressBar.left;
        }
        invalidate(mSeekBounds);

    }

    public static int constrainValue(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private static int dpToPx(DisplayMetrics displayMetrics, int dps) {
        return (int) (dps * displayMetrics.density + 0.5f);
    }

    public static int getDefaultBufferedColor(int playedColor) {
        return 0xCC000000 | (playedColor & 0x00FFFFFF);
    }

    public static int getDefaultScrubberColor(int playedColor) {
        return 0xFF000000 | playedColor;
    }

    public static int getDefaultUnplayedColor(int playedColor) {
        return 0x33000000 | (playedColor & 0x00FFFFFF);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        drawTimeBar(canvas);
        drawPlayhead(canvas);
        // Log.e("TAG/", "onDraw: ");
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {


//        Log.e("TAG/", "onLayout: " + "changed = " + changed +
//                "\n left = " + left +
//                "\n top = " + top +
//                "\n right = " + right +
//                "\n bottom = " + bottom +
//                "");

        int width = right - left;
        int height = bottom - top;
        int barY = (height - mTouchTargetHeight) / 2;
        int seekLeft = getPaddingLeft();
        int seekRight = width - getPaddingRight();
        int progressY = barY + (mTouchTargetHeight - mBarHeight) / 2;
//        Log.e("TAG/", "\n width = " + width +
//                "\n height = " + height +
//                "\n barY = " + barY +
//                "\n seekLeft = " + seekLeft +
//                "\n seekRight = " + seekRight +
//                "\n progressY = " + progressY);
        mSeekBounds.set(seekLeft, barY, seekRight, barY + mTouchTargetHeight);
        mProgressBar.set(mSeekBounds.left + mScrubberPadding, progressY,
                mSeekBounds.right - mScrubberPadding,
                progressY + mBarHeight);
        update();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // limit the height to {mTouchTargetHeight}
        // Log.e("TAG/", "onMeasure: " + "\nheightMeasureSpec = " + MeasureSpec.toString(heightMeasureSpec));

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int height;
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            height = mTouchTargetHeight;

        } else {
            if (heightMode == MeasureSpec.EXACTLY) height = heightSize;
            else height = Math.min(mTouchTargetHeight, heightSize);
        }
        // Log.e("TAG/", "onMeasure: " + "\nheight: " + height);

        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || mDuration <= 0) {
            return false;
        }
        Point touchPosition = resolveRelativeTouchPosition(event);
        int x = touchPosition.x;
        int y = touchPosition.y;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInSeekBar(x, y)) {
                    positionScrubber(x);
                    startScrubbing();
                    mScrubPosition = getScrubberPosition();
//                    if (mScrubPosition < 0) {
//
//                        Log.e("TAG/", "onTouchEvent: isInSeekBar(x, y) " + mScrubPosition
//                                + "\nx = " + x);
//
//                    }
                    update();
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsScrubbing) {
                    if (y < mFineScrubYThreshold) {
                        int relativeX = x - mLastCoarseScrubXPosition;
                        positionScrubber(mLastCoarseScrubXPosition + relativeX / FINE_SCRUB_RATIO);
                    } else {
                        mLastCoarseScrubXPosition = x;
                        positionScrubber(x);
                    }
                    mScrubPosition = getScrubberPosition();
                    for (OnScrubListener listener : mListeners) {
                        listener.onScrubMove(this, mScrubPosition);
                    }
                }
                update();
                invalidate();

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mIsScrubbing) {
                    stopScrubbing(event.getAction() == MotionEvent.ACTION_CANCEL);
                    return true;
                }
                break;
            default:
        }
        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (mIsScrubbing && !enabled) {
            stopScrubbing(true);
        }
    }

    public interface OnScrubListener {


        void onScrubMove(TimeBar timeBar, long position);

        void onScrubStart(TimeBar timeBar, long position);

        void onScrubStop(TimeBar timeBar, long position, boolean canceled);

    }
}
