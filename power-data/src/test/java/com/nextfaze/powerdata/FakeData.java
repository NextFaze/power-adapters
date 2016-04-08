package com.nextfaze.powerdata;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class FakeData<T> extends AbstractData<T> implements List<T> {

    @NonNull
    private final ArrayList<T> mData = new ArrayList<>();

    private boolean mNotificationsEnabled = true;

    private int mAvailable = UNKNOWN;

    private boolean mLoading;

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
        if (mNotificationsEnabled) {
            notifyItemRemoved(index);
        }
        return removed;
    }

    @Override
    public final boolean add(@NonNull T t) {
        if (mData.add(t)) {
            if (mNotificationsEnabled) {
                notifyItemInserted(mData.size() - 1);
            }
            return true;
        }
        return false;
    }

    @Override
    public final void add(int index, T object) {
        mData.add(index, object);
        if (mNotificationsEnabled) {
            notifyItemInserted(index);
        }
    }

    @Override
    public final boolean addAll(@NonNull Collection<? extends T> collection) {
        int oldSize = mData.size();
        mData.addAll(collection);
        int newSize = mData.size();
        if (newSize != oldSize) {
            int count = mData.size() - oldSize;
            if (mNotificationsEnabled) {
                notifyItemRangeInserted(oldSize, count);
            }
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
            if (mNotificationsEnabled) {
                notifyItemRangeInserted(index, count);
            }
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
            if (mNotificationsEnabled) {
                notifyItemRemoved(index);
            }
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
        if (removed && mNotificationsEnabled) {
            // TODO: Fine-grained change notification.
            notifyDataChanged();
        }
        return removed;
    }

    @Override
    public final boolean retainAll(@NonNull Collection<?> collection) {
        boolean changed = mData.retainAll(collection);
        if (changed && mNotificationsEnabled) {
            // TODO: Fine-grained change notification.
            notifyDataChanged();
        }
        return changed;
    }

    @Override
    public final T set(int index, T object) {
        T t = mData.set(index, object);
        if (mNotificationsEnabled) {
            notifyItemChanged(index);
        }
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
        int size = mData.size();
        if (size > 0) {
            mData.clear();
            if (mNotificationsEnabled) {
                notifyItemRangeRemoved(0, size);
            }
        }
    }

    public final void move(int fromPosition, int toPosition, int itemCount) {
        ArrayList<T> copy = new ArrayList<>();
        for (int i = 0; i < itemCount; i++) {
            copy.add(mData.remove(fromPosition));
        }
        for (int i = 0; i < itemCount; i++) {
            mData.add(toPosition + i, copy.get(i));
        }
        if (mNotificationsEnabled) {
            notifyItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void refresh() {
    }

    @Override
    public void reload() {
    }

    @Override
    public int available() {
        return mAvailable;
    }

    public void setAvailable(int available) {
        if (available != mAvailable) {
            mAvailable = available;
            notifyAvailableChanged();
        }
    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    public void setLoading(boolean loading) {
        if (loading != mLoading) {
            mLoading = loading;
            notifyLoadingChanged();
        }
    }

    public boolean isNotificationsEnabled() {
        return mNotificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        mNotificationsEnabled = notificationsEnabled;
    }

    @Override
    public void notifyError(@NonNull Throwable e) {
        super.notifyError(e);
    }
}
