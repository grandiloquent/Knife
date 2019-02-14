package euphoria.psycho.knife.photo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.MotionEventCompat;
import androidx.viewpager.widget.ViewPager;

public class PhotoViewPager extends ViewPager {
    public static enum InterceptType {NONE, LEFT, RIGHT, BOTH}

    public static interface OnInterceptTouchListener {

        public InterceptType onTouchIntercept(float origX, float origY);
    }


    private static final int INVALID_POINTER = -1;
    private float mLastMotionX;
    private int mActivePointerId;
    private float mActivatedX;
    private float mActivatedY;
    private OnInterceptTouchListener mListener;

    public PhotoViewPager(Context context) {
        super(context);
        initialize();
    }

    public PhotoViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setPageTransformer(true, new PageTransformer() {
            @Override
            public void transformPage(  View page, float position) {
                if (position < 0 || position >= 1.f) {
                    page.setTranslationX(0);
                    page.setAlpha(1.f);
                    page.setScaleX(1);
                    page.setScaleY(1);
                } else {
                    page.setTranslationX(-position * page.getWidth());
                    page.setAlpha(Math.max(0, 1.f - position));
                    final float scale = Math.max(0, 1.f - position * 0.3f);
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final InterceptType intercept = (mListener != null)
                ? mListener.onTouchIntercept(mActivatedX, mActivatedY)
                : InterceptType.NONE;
        final boolean ignoreScrollLeft =
                (intercept == InterceptType.BOTH || intercept == InterceptType.LEFT);
        final boolean ignoreScrollRight =
                (intercept == InterceptType.BOTH || intercept == InterceptType.RIGHT);
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mActivePointerId = INVALID_POINTER;
        }
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (ignoreScrollLeft || ignoreScrollRight) {
                    final int activePointerId = mActivePointerId;
                    if (activePointerId == INVALID_POINTER) {
                        break;
                    }
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    if (ignoreScrollLeft && ignoreScrollRight) {
                        mLastMotionX = x;
                        return false;
                    } else if (ignoreScrollLeft && (x > mLastMotionX)) {
                        mLastMotionX = x;
                        return false;
                    } else if (ignoreScrollRight && (x < mLastMotionX)) {
                        mLastMotionX = x;
                        return false;
                    }
                }

                break;
            }
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = ev.getX();
                mActivatedX = ev.getRawX();
                mActivatedY = ev.getRawY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP: {
                final int pointerIndex = MotionEventCompat.getActionIndex(ev);
                final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastMotionX = MotionEventCompat.getX(ev, newPointerIndex);
                    mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnInterceptTouchListener(OnInterceptTouchListener l) {
        mListener = l;
    }
}