package com.nextfaze.databind;

import com.nextfaze.concurrent.Task;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * Simple mutable {@link Data} implementation backed by an {@link ArrayList}. Cannot contain {@code null} elements. Not
 * thread-safe.
 * @param <T> The type of element this data contains.
 */
@Accessors(prefix = "m")
public abstract class ArrayData<T> extends AbstractData<T> implements MutableData<T> {

    private static final Logger log = LoggerFactory.getLogger(ArrayData.class);

    @NonNull
    private final ArrayList<T> mData = new ArrayList<>();

    @Nullable
    private Task<?> mTask;

    /** Automatically invalidate contents if data is hidden for the specified duration. */
    @Getter
    @Setter
    private long mAutoInvalidateDelay = Long.MAX_VALUE;

    private boolean mDirty = true;
    private int mAvailable = Integer.MAX_VALUE;

    protected ArrayData() {
    }

    /** Subclasses must call through to super. */
    @Override
    protected void onClose() throws Throwable {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
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
        notifyDataChanged();
        return removed;
    }

    @Override
    public final boolean add(@NonNull T t) {
        if (mData.add(t)) {
            notifyDataChanged();
            return true;
        }
        return false;
    }

    @Override
    public final void add(int index, T object) {
        mData.add(index, object);
        notifyDataChanged();
    }

    @Override
    public final boolean addAll(Collection<? extends T> collection) {
        boolean changed = mData.addAll(collection);
        if (changed) {
            notifyDataChanged();
        }
        return changed;
    }

    @Override
    public final boolean addAll(int index, Collection<? extends T> collection) {
        boolean changed = mData.addAll(index, collection);
        if (changed) {
            notifyDataChanged();
        }
        return changed;
    }

    @Override
    public final boolean remove(@NonNull Object obj) {
        if (mData.remove(obj)) {
            notifyDataChanged();
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
            notifyDataChanged();
        }
        return removed;
    }

    @Override
    public final boolean retainAll(@NonNull Collection<?> collection) {
        boolean changed = mData.retainAll(collection);
        if (changed) {
            notifyDataChanged();
        }
        return changed;
    }

    @Override
    public final T set(int index, T object) {
        T t = mData.set(index, object);
        notifyDataChanged();
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
        mData.clear();
        setAvailable(Integer.MAX_VALUE);
        notifyDataChanged();
        invalidate();
    }

    /** Marks the existing loaded elements as dirty, such that they will be reloaded as soon as data is next shown. */
    public final void invalidate() {
        mDirty = true;
        loadDataIfAppropriate();
    }

    @Override
    public final boolean isLoading() {
        return mTask != null;
    }

    @Override
    public final int available() {
        return mAvailable;
    }

    @NonNull
    protected abstract List<? extends T> load() throws Throwable;

    @Override
    protected final void onShown(long millisHidden) {
        if (millisHidden >= mAutoInvalidateDelay) {
            log.trace("Automatically invalidating due to auto-invalidate delay being reached or exceeded");
            mDirty = true;
        }
        loadDataIfAppropriate();
    }

    @Override
    protected final void onHidden(long millisShown) {
    }

    @Override
    protected final void onHideTimeout() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    private void loadDataIfAppropriate() {
        // We only start loading the data if it's not already loading, and we're shown.
        // If we're not shown we don't care about the data.
        // Only load if data is marked as dirty.
        if (mDirty && mTask == null && isShown()) {
            // TODO: Replace use of Task, so we stop depending on NextFaze Concurrent library.
            mTask = new Task<List<? extends T>>() {
                @Override
                protected List<? extends T> call() throws Throwable {
                    return load();
                }

                @Override
                protected void onSuccess(@NonNull List<? extends T> data) throws Throwable {
                    mData.clear();
                    for (T t : data) {
                        if (t != null) {
                            mData.add(t);
                        }
                    }
                    mTask = null;
                    setAvailable(0);
                    notifyLoadingChanged();
                    notifyDataChanged();
                }

                @Override
                protected void onCanceled() throws Throwable {
                    mTask = null;
                    notifyLoadingChanged();
                }

                @Override
                protected void onFailure(@NonNull Throwable e) throws Throwable {
                    // TODO: Remain dirty. Should reload next opportunity if this load failed.
                    mDirty = false;
                    mTask = null;
                    notifyLoadingChanged();
                    notifyError(e);
                }

                @Override
                protected void onFinally() throws Throwable {
                    loadDataIfAppropriate();
                }
            };
            notifyLoadingChanged();
            mTask.execute();
        }
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
