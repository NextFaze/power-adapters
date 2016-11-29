package com.nextfaze.poweradapters.data;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import lombok.NonNull;

import java.io.InterruptedIOException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

import static android.os.Looper.getMainLooper;

abstract class Task<T> {

    private static final String TAG = Task.class.getSimpleName();

    private interface Action {
        void run() throws Throwable;
    }

    @NonNull
    static Executor sExecutor = AsyncTask.THREAD_POOL_EXECUTOR;

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final FutureTask<?> mFutureTask = new FutureTask<>(new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            mExecutingThread = Thread.currentThread();
            try {
                if (isCanceled()) {
                    return null;
                }
                doPreExecute();
                if (isCanceled()) {
                    return null;
                }
                T result = doCall();
                if (isCanceled()) {
                    return null;
                }
                doSuccess(result);
            } catch (Throwable e) {
                if (!isCanceled()) {
                    doFailure(e);
                }
            } finally {
                mExecutingThread = null;
            }
            return null;
        }
    });

    @Nullable
    private volatile Thread mExecutingThread;

    private boolean mCanceled;
    private volatile boolean mExecuted;

    private void doPreExecute() throws Throwable {
        post(new Action() {
            @Override
            public void run() throws Throwable {
                onPreExecute();
            }
        });
    }

    private T doCall() throws Throwable {
        try {
            return Task.this.call();
        } catch (InterruptedException | InterruptedIOException e) {
            // Ignore interruptions.
            throw e;
        } catch (Throwable e) {
            logError("call", e);
            throw e;
        }
    }

    private void doSuccess(final T result) {
        try {
            post(new Action() {
                @Override
                public void run() throws Throwable {
                    onSuccess(result);
                }
            });
            doFinally();
        } catch (InterruptedException | InterruptedIOException e) {
            // Ignore interruptions.
        } catch (Throwable e) {
            if (!isCanceled()) {
                logError("onSuccess", e);
                doFailure(e);
            }
        }
    }

    private void doFailure(final Throwable e) {
        try {
            post(new Action() {
                @Override
                public void run() throws Throwable {
                    onFailure(e);
                }
            });
        } catch (Throwable t) {
            logError("onFailure", t);
        } finally {
            doFinally();
        }
    }

    private void doCanceled() {
        try {
            post(new Action() {
                @Override
                public void run() throws Throwable {
                    onCanceled();
                }
            });
            doFinally();
        } catch (Throwable e) {
            logError("onCanceled", e);
            doFailure(e);
        }
    }

    private void doFinally() {
        try {
            post(new Action() {
                @Override
                public void run() throws Throwable {
                    onFinally();
                }
            });
        } catch (InterruptedException | InterruptedIOException e) {
            // Ignore interruptions.
        } catch (Throwable e) {
            logError("onFinally", e);
        }
    }

    Task() {
    }

    @NonNull
    Task<T> execute() {
        synchronized (this) {
            mCanceled = false;
        }
        mExecuted = true;
        sExecutor.execute(mFutureTask);
        return this;
    }

    /**
     * Calls {@link #cancel(boolean)} with a value of <code>true</code>.
     * @return <code>true</code> if the task was canceled, otherwise <code>false</code>.
     * @see #cancel(boolean)
     */
    boolean cancel() {
        return cancel(true);
    }

    /**
     * Cancels the task, as long as it was executed to begin with. The {@link #onCanceled()} callback will be invoked
     * here if the task was indeed canceled.
     * @param mayInterruptIfRunning Interrupts the running thread if <code>true</code>.
     * @return <code>true</code> if the task was canceled, otherwise <code>false</code>.
     */
    boolean cancel(boolean mayInterruptIfRunning) {
        if (!isExecuted()) {
            return false;
        }

        synchronized (this) {
            if (isCanceled()) {
                return false;
            }
            mCanceled = true;
        }

        try {
            mFutureTask.cancel(mayInterruptIfRunning);
        } catch (Throwable e) {
            // We don't care about why cancel threw.
        }

        doCanceled();
        return true;
    }

    boolean isCanceled() {
        synchronized (this) {
            return mCanceled;
        }
    }

    boolean isExecuted() {
        return mExecuted;
    }

    /** Called to perform the work in the background. May return a <code>null</code> result */
    protected abstract T call() throws Throwable;

    /** Called before every other callback, if the task was executed at all. */
    protected void onPreExecute() throws Throwable {
    }

    /** Called if {@link #call()} returns normally, possibly with a <code>null</code> result. */
    protected void onSuccess(T t) throws Throwable {
    }

    /** Called after {@link #call()} or any other callback throws an exception. */
    protected void onFailure(@NonNull Throwable e) throws Throwable {
    }

    /** Called immediately after {@link #cancel(boolean)}. */
    protected void onCanceled() throws Throwable {
    }

    /** Called after all other callbacks, as long as the task was executed at all. */
    protected void onFinally() throws Throwable {
    }

    private void post(final Action action) throws Throwable {
        if (!isHandlerThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            final Throwable[] ex = new Throwable[1];
            mHandler.post(new Runnable() {
                public void run() {
                    try {
                        action.run();
                    } catch (Throwable e) {
                        ex[0] = e;
                    } finally {
                        latch.countDown();
                    }
                }
            });
            latch.await();
            if (ex[0] != null) {
                throw ex[0];
            }
        } else {
            action.run();
        }
    }

    private boolean isHandlerThread() {
        return Looper.myLooper() == mHandler.getLooper();
    }

    private void logError(String callbackName, Throwable e) {
        Log.e(TAG, callbackName + " error", e);
    }
}
