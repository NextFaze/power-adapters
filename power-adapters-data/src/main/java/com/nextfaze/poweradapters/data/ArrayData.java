package com.nextfaze.poweradapters.data;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.internal.NotifyingArrayList;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Simple mutable {@link Data} implementation backed by an {@link ArrayList}. Cannot contain {@code null} elements. Not
 * thread-safe.
 * @param <T> The type of element this data contains.
 */
public abstract class ArrayData<T> extends SimpleData<T, List<? extends T>> implements Closeable {

    /** The backing array of non-null elements. */
    @NonNull
    private final NotifyingArrayList<T> mData = new NotifyingArrayList<>(mDataObservable);

    protected ArrayData() {
        this(DataExecutors.defaultExecutor());
    }

    protected ArrayData(@NonNull ExecutorService executor) {
        super(executor);
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
        return mData.get(position);
    }

    /**
     * Returns this {@link ArrayData} as a mutable list. Operations performed on the returned {@link List} are reflected
     * in this {@link ArrayData}, and the correct notifications will be issued.
     */
    @NonNull
    @Override
    public final List<T> asList() {
        return mData;
    }

    /** Called in a background thread to load the data set. */
    @NonNull
    protected abstract List<? extends T> load() throws Throwable;

    @NonNull
    @Override
    final List<? extends T> loadDataSet() throws Throwable {
        return load();
    }

    @Override
    final void onNewDataSet(@Nullable List<? extends T> newDataSet) {
        if (newDataSet != null) {
            mData.replaceAll(newDataSet);
        } else {
            mData.clear();
        }
    }
}
