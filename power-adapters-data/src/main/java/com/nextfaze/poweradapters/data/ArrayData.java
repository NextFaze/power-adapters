package com.nextfaze.poweradapters.data;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.internal.NotifyingArrayList;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple mutable {@link Data} implementation backed by an {@link ArrayList}. Cannot contain {@code null} elements. Not
 * thread-safe.
 * @param <T> The type of element this data contains.
 */
public abstract class ArrayData<T> extends Data<T> implements Closeable {

    /** The backing array of non-null elements. */
    @NonNull
    private final NotifyingArrayList<T> mData = new NotifyingArrayList<>(mDataObservable);

    @Nullable
    private Task<?> mTask;

    /** Indicates the currently loaded data is invalid and needs to be reloaded next opportunity. */
    private boolean mDirty = true;

    /** Causes elements to be cleared next time we activate. */
    private boolean mClear;

    /** Indicates more elements are currently loading. */
    private boolean mLoading;

    /** @see #available() */
    private int mAvailable = Integer.MAX_VALUE;

    protected ArrayData() {
    }

    @CallSuper
    @Override
    public void close() {
        cancelTask();
        mData.clear();
        mData.trimToSize();
    }

    @Override
    public final int size() {
        return mData.size();
    }

    @NonNull
    @Override
    public final T get(int position, int flags) {
        //noinspection ConstantConditions
        return mData.get(position);
    }

    public final void clear() {
        onClear();
        mData.clear();
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

    /**
     * Returns this {@link ArrayData} as a mutable list. Operations performed on the returned {@link List} are reflected
     * in this {@link ArrayData}, and the correct notifications will be issued.
     */
    @NonNull
    @Override
    public List<T> asList() {
        return mData;
    }

    /** Called in a background thread to load the data set. */
    @NonNull
    protected abstract List<? extends T> load() throws Throwable;

    /** Called prior to elements being cleared. Always called from the UI thread. */
    @SuppressWarnings("WeakerAccess")
    protected void onClear() {
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
        // We only start loading the data if it's not already loading, and we're shown.
        // If we're not shown we don't care about the data.
        // Only load if data is marked as dirty.
        if (mDirty && mTask == null && getDataObserverCount() > 0) {
            // TODO: Replace use of Task with either a plain Thread or use of an Executor.
            mTask = new Task<List<? extends T>>() {
                @Override
                protected List<? extends T> call() throws Throwable {
                    return load();
                }

                @Override
                protected void onSuccess(@NonNull List<? extends T> data) throws Throwable {
                    onClear();
                    mDirty = false;
                    mClear = false;
                    mData.replaceAll(data);
                    setAvailable(0);
                    mTask = null;
                    loadDataIfAppropriate();
                    updateLoading();
                }

                @Override
                protected void onFailure(@NonNull Throwable e) throws Throwable {
                    mTask = null;
                    updateLoading();
                    notifyError(e);
                }
            };
            mTask.execute();
        }
    }

    private void cancelTask() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    private void updateLoading() {
        setLoading(mTask != null);
    }

    private void setLoading(final boolean loading) {
        if (mLoading != loading) {
            mLoading = loading;
            notifyLoadingChanged();
        }
    }

    private void setAvailable(final int available) {
        if (mAvailable != available) {
            mAvailable = available;
            notifyAvailableChanged();
        }
    }
}
