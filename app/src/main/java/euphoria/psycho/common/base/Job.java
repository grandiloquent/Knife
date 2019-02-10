package euphoria.psycho.common.base;


import android.os.CancellationSignal;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public abstract class Job implements Runnable {

    static final int STATE_CANCELED = 4;
    static final int STATE_COMPLETED = 3;
    static final int STATE_CREATED = 0;
    static final int STATE_SET_UP = 2;
    static final int STATE_STARTED = 1;
    private final Listener mListener;
    final CancellationSignal mSignal = new CancellationSignal();
    private volatile @State
    int mState = STATE_CREATED;

    protected Job(Listener listener) {
        mListener = listener;
    }

    public final void cancel() {
        mState = STATE_CANCELED;
        mSignal.cancel();
    }

    protected abstract void finish();

    public final @State
    int getState() {
        return mState;
    }

    public final boolean isCanceled() {
        return mState == STATE_CANCELED;
    }

    public final boolean isFinished() {
        return mState == STATE_CANCELED || mState == STATE_COMPLETED;
    }

    protected boolean setUp() {
        return true;
    }

    protected abstract void start();

    @Override
    public void run() {
        if (isCanceled()) {
            // Canceled before running
            return;
        }

        mState = STATE_STARTED;
        mListener.onStart(this);

        try {
            boolean result = setUp();
            if (result && !isCanceled()) {
                mState = STATE_SET_UP;
                start();
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            Log.e("TAG/", "run: " + e.getMessage());

            // No exceptions should be thrown here, as all calls to the provider must be
            // handled within Job implementations. However, just in case catch them here.
        } finally {
            mState = (mState == STATE_STARTED || mState == STATE_SET_UP) ? STATE_COMPLETED : mState;
            finish();
            mListener.onFinished(this);

        }
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_CREATED, STATE_STARTED, STATE_SET_UP, STATE_COMPLETED, STATE_CANCELED})
    @interface State {
    }

    public interface Listener {
        void onFinished(Job job);

        void onStart(Job job);
    }
}
