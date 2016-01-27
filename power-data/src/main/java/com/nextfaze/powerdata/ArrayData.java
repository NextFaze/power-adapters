package com.nextfaze.powerdata;

import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * Simple mutable {@link Data} implementation backed by an {@link ArrayList}. Cannot contain {@code null} elements. Not
 * thread-safe.
 * @param <T> The type of element this data contains.
 */
@Accessors(prefix = "m")
public abstract class ArrayData<T> extends AbstractData<T> implements List<T> {

    /** The backing array of non-null elements. */
    @NonNull
    private final ArrayList<T> mData = new ArrayList<>();

    /**
     * Presence of this task indicates loading state. Changes to this field must be accompanied by {@link
     * #notifyLoadingChanged()}.
     */
    @Nullable
    private Task<?> mTask;

    /** Indicates the currently loaded data is invalid and needs to be reloaded next opportunity. */
    private boolean mDirty = true;

    /** Causes elements to be cleared next time we become shown. */
    private boolean mClear;

    /** Indicates more elements are currently loading. */
    private boolean mLoading;

    /** @see #available() */
    private int mAvailable = Integer.MAX_VALUE;

    protected ArrayData() {
    }

    @CallSuper
    public void close() {
        cancelTask();
        mData.clear();
        mData.trimToSize();
    }

    @Override
    public final int size() {
        return mData.size();
    }

    @Override
    public final boolean isEmpty() {
        return mData.isEmpty();
    }

    @Override
    public final boolean contains(Object object) {
        return mData.contains(object);
    }

    @Override
    public final int indexOf(Object object) {
        return mData.indexOf(object);
    }

    @Override
    public final int lastIndexOf(Object object) {
        return mData.lastIndexOf(object);
    }

    @Override
    public final T remove(int index) {
        T removed = mData.remove(index);
        notifyItemRemoved(index);
        return removed;
    }

    @Override
    public final boolean add(@NonNull T t) {
        if (mData.add(t)) {
            notifyItemInserted(mData.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public final void add(int index, T object) {
        mData.add(index, object);
        notifyItemInserted(index);
    }

    @Override
    public final boolean addAll(@NonNull Collection<? extends T> collection) {
        int oldSize = mData.size();
        mData.addAll(collection);
        int newSize = mData.size();
        if (newSize != oldSize) {
            int count = mData.size() - oldSize;
            notifyItemRangeInserted(oldSize, count);
            return true;
        }
        return false;
    }

    @Override
    public final boolean addAll(int index, @NonNull Collection<? extends T> collection) {
        int oldSize = mData.size();
        mData.addAll(index, collection);
        int newSize = mData.size();
        if (newSize != oldSize) {
            int count = mData.size() - oldSize;
            notifyItemRangeInserted(index, count);
            return true;
        }
        return false;
    }

    @Override
    public final boolean remove(@NonNull Object obj) {
        //noinspection SuspiciousMethodCalls
        int index = mData.indexOf(obj);
        if (index != -1) {
            mData.remove(index);
            notifyItemRemoved(index);
            return true;
        }
        return false;
    }

    // TODO: Notify of change if modified from iterator.

    @NonNull
    @Override
    public final ListIterator<T> listIterator() {
        return mData.listIterator();
    }

    @NonNull
    @Override
    public final ListIterator<T> listIterator(int location) {
        return mData.listIterator(location);
    }

    @NonNull
    @Override
    public final List<T> subList(int start, int end) {
        return mData.subList(start, end);
    }

    @Override
    public final boolean containsAll(@NonNull Collection<?> collection) {
        return mData.containsAll(collection);
    }

    @Override
    public final boolean removeAll(@NonNull Collection<?> collection) {
        boolean removed = mData.removeAll(collection);
        if (removed) {
            // TODO: Fine-grained change notification.
            notifyDataChanged();
        }
        return removed;
    }

    @Override
    public final boolean retainAll(@NonNull Collection<?> collection) {
        boolean changed = mData.retainAll(collection);
        if (changed) {
            // TODO: Fine-grained change notification.
            notifyDataChanged();
        }
        return changed;
    }

    @Override
    public final T set(int index, T object) {
        T t = mData.set(index, object);
        notifyItemChanged(index);
        return t;
    }

    @NonNull
    @Override
    public final Object[] toArray() {
        return mData.toArray();
    }

    @NonNull
    @Override
    public final <T> T[] toArray(@NonNull T[] contents) {
        return mData.toArray(contents);
    }

    @NonNull
    @Override
    public final T get(int position, int flags) {
        //noinspection ConstantConditions
        return mData.get(position);
    }

    @Override
    public final void clear() {
        onClear();
        int size = mData.size();
        if (size > 0) {
            mData.clear();
            setAvailable(Integer.MAX_VALUE);
            notifyItemRangeRemoved(0, size);
        }
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

    /** Called in a background thread to load the data set. */
    @NonNull
    protected abstract List<? extends T> load() throws Throwable;

    /** Called prior to elements being cleared. Always called from the UI thread. */
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
                    int oldSize = mData.size();
                    int newSize = data.size();
                    int deltaSize = newSize - oldSize;

                    mData.clear();
                    for (T t : data) {
                        if (t != null) {
                            mData.add(t);
                        }
                    }

                    int changed = min(oldSize, newSize);
                    if (changed > 0) {
                        notifyItemRangeChanged(0, changed);
                    }
                    if (deltaSize < 0) {
                        notifyItemRangeRemoved(oldSize + deltaSize, abs(deltaSize));
                    } else if (deltaSize > 0) {
                        notifyItemRangeInserted(oldSize, abs(deltaSize));
                    }
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mLoading != loading) {
                    mLoading = loading;
                    notifyLoadingChanged();
                }
            }
        });
    }

    private void setAvailable(final int available) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAvailable != available) {
                    mAvailable = available;
                    notifyAvailableChanged();
                }
            }
        });
    }
}
