package com.nextfaze.poweradapters;

import lombok.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Math.min;

/** @hide Not intended for public use. */
public final class NotifyingArrayList<E> extends AbstractList<E> {

    @NonNull
    private final DataObservable mDataObservable;

    @NonNull
    private final ArrayList<E> mArray = new ArrayList<>();

    public NotifyingArrayList(@NonNull DataObservable dataObservable) {
        mDataObservable = dataObservable;
    }

    @Override
    public E get(int location) {
        return mArray.get(location);
    }

    @Override
    public int size() {
        return mArray.size();
    }

    @Override
    public E set(int index, @NonNull E object) {
        E e = mArray.set(index, object);
        mDataObservable.notifyItemChanged(index);
        return e;
    }

    @Override
    public boolean add(@NonNull E e) {
        if (mArray.add(e)) {
            mDataObservable.notifyItemInserted(mArray.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, @NonNull E object) {
        mArray.add(index, object);
        mDataObservable.notifyItemInserted(index);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        int oldSize = mArray.size();
        mArray.addAll(collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mDataObservable.notifyItemRangeInserted(oldSize, count);
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends E> collection) {
        int oldSize = mArray.size();
        mArray.addAll(index, collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mDataObservable.notifyItemRangeInserted(index, count);
            return true;
        }
        return false;
    }

    @Override
    public E remove(int index) {
        E removed = mArray.remove(index);
        mDataObservable.notifyItemRemoved(index);
        return removed;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(@NonNull Object obj) {
        int index = mArray.indexOf(obj);
        if (index != -1) {
            mArray.remove(index);
            mDataObservable.notifyItemRemoved(index);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        int size = mArray.size();
        if (size > 0) {
            mArray.clear();
            mDataObservable.notifyItemRangeRemoved(0, size);
        }
    }

    public void trimToSize() {
        mArray.trimToSize();
    }

    public void ensureCapacity(int minimumCapacity) {
        mArray.ensureCapacity(minimumCapacity);
    }

    public void replaceAll(@NonNull Collection<? extends E> collection) {
        int oldSize = mArray.size();
        int newSize = collection.size();
        int deltaSize = newSize - oldSize;
        mArray.clear();
        for (E e : collection) {
            if (e != null) {
                mArray.add(e);
            }
        }
        int changed = min(oldSize, newSize);
        if (changed > 0) {
            mDataObservable.notifyItemRangeChanged(0, changed);
        }
        if (deltaSize < 0) {
            mDataObservable.notifyItemRangeRemoved(oldSize + deltaSize, -deltaSize);
        } else if (deltaSize > 0) {
            mDataObservable.notifyItemRangeInserted(oldSize, deltaSize);
        }
    }
}
