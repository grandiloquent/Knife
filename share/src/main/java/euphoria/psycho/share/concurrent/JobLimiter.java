package euphoria.psycho.share.concurrent;

import android.util.Log;

import java.util.LinkedList;

import euphoria.psycho.share.concurrent.ThreadPool.Job;
import euphoria.psycho.share.concurrent.ThreadPool.JobContext;
import euphoria.psycho.share.util.Utils;

// Limit the number of concurrent jobs that has been submitted into a ThreadPool
@SuppressWarnings("rawtypes")
public class JobLimiter implements FutureListener {
    private static final String TAG = "JobLimiter";

    // State Transition:
    //      INIT -> DONE, CANCELLED
    //      DONE -> CANCELLED
    private static final int STATE_INIT = 0;
    private static final int STATE_DONE = 1;
    private static final int STATE_CANCELLED = 2;

    private final LinkedList<JobWrapper<?>> mJobs = new LinkedList<JobWrapper<?>>();
    private final ThreadPool mPool;
    private int mLimit;

    private static class JobWrapper<T> implements Future<T>, Job<T> {
        private int mState = STATE_INIT;
        private Job<T> mJob;
        private Future<T> mDelegate;
        private FutureListener<T> mListener;
        private T mResult;

        public JobWrapper(Job<T> job, FutureListener<T> listener) {
            mJob = job;
            mListener = listener;
        }

        public synchronized void setFuture(Future<T> future) {
            if (mState != STATE_INIT) return;
            mDelegate = future;
        }

        @Override
        public void cancel() {
            FutureListener<T> listener = null;
            synchronized (this) {
                if (mState != STATE_DONE) {
                    listener = mListener;
                    mJob = null;
                    mListener = null;
                    if (mDelegate != null) {
                        mDelegate.cancel();
                        mDelegate = null;
                    }
                }
                mState = STATE_CANCELLED;
                mResult = null;
                notifyAll();
            }
            if (listener != null) listener.onFutureDone(this);
        }

        @Override
        public synchronized boolean isCancelled() {
            return mState == STATE_CANCELLED;
        }

        @Override
        public boolean isDone() {
            // Both CANCELLED AND DONE is considered as done
            return mState !=  STATE_INIT;
        }

        @Override
        public synchronized T get() {
            while (mState == STATE_INIT) {
                // handle the interrupted exception of wait()
                Utils.waitWithoutInterrupt(this);
            }
            return mResult;
        }

        @Override
        public void waitDone() {
            get();
        }

        @Override
        public T run(JobContext jc) {
            Job<T> job = null;
            synchronized (this) {
                if (mState == STATE_CANCELLED) return null;
                job = mJob;
            }
            T result  = null;
            try {
                result = job.run(jc);
            } catch (Throwable t) {
                Log.w(TAG, "error executing job: " + job, t);
            }
            FutureListener<T> listener = null;
            synchronized (this) {
                if (mState == STATE_CANCELLED) return null;
                mState = STATE_DONE;
                listener = mListener;
                mListener = null;
                mJob = null;
                mResult = result;
                notifyAll();
            }
            if (listener != null) listener.onFutureDone(this);
            return result;
        }
    }

    public JobLimiter(ThreadPool pool, int limit) {
        mPool = Utils.checkNotNull(pool);
        mLimit = limit;
    }

    public synchronized <T> Future<T> submit(Job<T> job, FutureListener<T> listener) {
        JobWrapper<T> future = new JobWrapper<T>(Utils.checkNotNull(job), listener);
        mJobs.addLast(future);
        submitTasksIfAllowed();
        return future;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void submitTasksIfAllowed() {
        while (mLimit > 0 && !mJobs.isEmpty()) {
            JobWrapper wrapper = mJobs.removeFirst();
            if (!wrapper.isCancelled()) {
                --mLimit;
                wrapper.setFuture(mPool.submit(wrapper, this));
            }
        }
    }

    @Override
    public synchronized void onFutureDone(Future future) {
        ++mLimit;
        submitTasksIfAllowed();
    }
}
