package com.nextfaze.powerdata;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

import static android.os.Looper.getMainLooper;

@Accessors(prefix = "m")
abstract class Task<T> {

    private static final String TAG = Task.class.getSimpleName();

    private interface Action {
        void run() throws Throwable;
    }

    @NonNull
    private static Executor sExecutor = AsyncTask.THREAD_POOL_EXECUTOR;

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
                doWorkStarted();
                T result;
                try {
                    result = doCall();
                } finally {
                    doWorkEnded();
                }
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

    @Getter
    @Nullable
    private volatile Thread mExecutingThread;

    @Getter
    @Setter
    private boolean mStackTraceEnabled = true;

    @Getter
    private Executor mExecutor;

    @Getter
    private StackTraceElement[] mLaunchStackTrace;

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

    private void doWorkStarted() {
        try {
            post(new Action() {
                @Override
                public void run() throws Throwable {
                    onWorkStarted();
                }
            });
        } catch (InterruptedException | InterruptedIOException e) {
            // Ignore interruptions.
        } catch (Throwable e) {
            logError("onWorkStarted", e);
        }
    }

    private void doWorkEnded() {
        try {
            post(new Action() {
                @Override
                public void run() throws Throwable {
                    onWorkEnded();
                }
            });
        } catch (InterruptedException | InterruptedIOException e) {
            // Ignore interruptions.
        } catch (Throwable e) {
            logError("onWorkEnded", e);
        }
    }

    public static void setDefaultExecutor(@NonNull ExecutorService executor) {
        sExecutor = executor;
    }

    public Task() {
        executor(sExecutor);
    }

    @NonNull
    public Task<T> executor(@Nullable Executor executor) {
        mExecutor = executor;
        return this;
    }

    @NonNull
    public Task<T> execute() {
        if (mStackTraceEnabled) {
            return execute(Thread.currentThread().getStackTrace());
        } else {
            return execute(null);
        }
    }

    @NonNull
    private Task<T> execute(@Nullable StackTraceElement[] launchStackTrace) {
        mLaunchStackTrace = launchStackTrace;
        synchronized (this) {
            mCanceled = false;
        }
        mExecuted = true;
        mExecutor.execute(mFutureTask);
        return this;
    }

    /**
     * Calls {@link #cancel(boolean)} with a value of <code>true</code>.
     * @return <code>true</code> if the task was canceled, otherwise <code>false</code>.
     * @see #cancel(boolean)
     */
    public boolean cancel() {
        return cancel(true);
    }

    /**
     * Cancels the task, as long as it was executed to begin with. The {@link #onCanceled()} callback will be invoked
     * here if the task was indeed canceled.
     * @param mayInterruptIfRunning Interrupts the running thread if <code>true</code>.
     * @return <code>true</code> if the task was canceled, otherwise <code>false</code>.
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
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

        // TODO: By invoking onCanceled here, it's possible that onWorkEnded will be called at the end instead of straight after onWorkStarted.

        doCanceled();
        return true;
    }

    public boolean isCanceled() {
        synchronized (this) {
            return mCanceled;
        }
    }

    public boolean isExecuted() {
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

    /** Called immediately before {@link #call()} to indicate the actual work is beginning. */
    protected void onWorkStarted() throws Throwable {
    }

    /** Called immediately after {@link #call()}, regardless of its success, to indicate the actual work is finishing. */
    protected void onWorkEnded() throws Throwable {
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
        if (mLaunchStackTrace != null) {
            concatStackTrace(e, mLaunchStackTrace);
        }
        Log.e(TAG, callbackName + " error", e);
    }

    private static void concatStackTrace(Throwable e, StackTraceElement[] stackTraceElements) {
        ArrayList<StackTraceElement> stack = new ArrayList<>(Arrays.asList(e.getStackTrace()));
        stack.addAll(Arrays.asList(stackTraceElements));
        e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
    }
}
