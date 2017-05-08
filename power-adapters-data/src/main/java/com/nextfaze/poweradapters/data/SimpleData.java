package com.nextfaze.poweradapters.data;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.InterruptedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

abstract class SimpleData<T, D> extends Data<T> {
    /** Executor used to perform load tasks. */
    @NonNull
    private final ExecutorService mExecutor;
    
    /** Future of the currently running load task. */
    @Nullable
    private Future<?> mFuture;

    /** Indicates if the current future is cancelled. */
    @Nullable
    private AtomicBoolean mCancelled;
    
    /** Indicates the currently loaded data set is invalid and needs to be reloaded next opportunity. */
    private boolean mDirty = true;

    /** Causes the data set to be cleared next time we're observed. */
    private boolean mClear;

    /** Indicates a new data set is currently loading. */
    private boolean mLoading;

    /** @see #available() */
    private int mAvailable = Integer.MAX_VALUE;

    SimpleData(@NonNull ExecutorService executor) {
        mExecutor = checkNotNull(executor, "executor");
    }

    public final void clear() {
        onNewDataSet(null);
        setAvailable(Integer.MAX_VALUE);
        mClear = false;
    }

    @Override
    public final void refresh() {
        mDirty = true;
        cancelTask();
        loadDataIfAppropriate();
        updateLoading();
    }

    @Override
    public final void reload() {
        clear();
        refresh();
        updateLoading();
    }

    @Override
    public final void invalidate() {
        cancelTask();
        mDirty = true;
        mClear = true;
        updateLoading();
    }

    @Override
    public final boolean isLoading() {
        return mLoading;
    }

    @Override
    public final int available() {
        return mAvailable;
    }

    @CallSuper
    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        if (mClear) {
            clear();
        }
        loadDataIfAppropriate();
        updateLoading();
    }

    private void loadDataIfAppropriate() {
        // We only start loading the new data set if it's not already loading, and we have observers.
        // Additionally only load if data is marked as dirty, so this may be invoked several times without
        // triggering multiple unnecessary loads.
        if (mDirty && mFuture == null && getDataObserverCount() > 0) {
            final AtomicBoolean cancelled = new AtomicBoolean();
            mCancelled = cancelled;
            mFuture = mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final D dataSet = loadDataSet();
                        if (!cancelled.get()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onLoadSuccess(dataSet);
                                }
                            });
                        }
                    } catch (InterruptedException | InterruptedIOException e) {
                        // Ignore
                    } catch (final Throwable e) {
                        if (!cancelled.get()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onLoadFailure(e);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    void onLoadSuccess(@NonNull D dataSet) {
        mDirty = false;
        mClear = false;
        onNewDataSet(dataSet);
        setAvailable(0);
        mFuture = null;
        loadDataIfAppropriate();
        updateLoading();
    }

    void onLoadFailure(@NonNull Throwable e) {
        mFuture = null;
        updateLoading();
        notifyError(e);
    }

    private void updateLoading() {
        setLoading(mFuture != null);
    }

    final void cancelTask() {
        if (mCancelled != null) {
            mCancelled.set(true);
            mCancelled = null;
        }
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }
    }

    private void setLoading(boolean loading) {
        if (mLoading != loading) {
            mLoading = loading;
            notifyLoadingChanged();
        }
    }

    private void setAvailable(int available) {
        if (mAvailable != available) {
            mAvailable = available;
            notifyAvailableChanged();
        }
    }

    @NonNull
    abstract D loadDataSet() throws Throwable;

    abstract void onNewDataSet(@Nullable D newDataSet);
}
