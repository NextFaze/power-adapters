package com.nextfaze.poweradapters.data;

import lombok.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;

final class NotifyingArrayList<E> extends AbstractList<E> {

    @NonNull
    private final Data<E> mData;

    @NonNull
    private final ArrayList<E> mArray = new ArrayList<>();

    NotifyingArrayList(@NonNull Data<E> data) {
        mData = data;
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
        mData.notifyItemChanged(index);
        return e;
    }

    @Override
    public boolean add(@NonNull E e) {
        if (mArray.add(e)) {
            mData.notifyItemInserted(mArray.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, @NonNull E object) {
        mArray.add(index, object);
        mData.notifyItemInserted(index);
    }

    @Override
    public final boolean addAll(@NonNull Collection<? extends E> collection) {
        int oldSize = mArray.size();
        mArray.addAll(collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mData.notifyItemRangeInserted(oldSize, count);
            return true;
        }
        return false;
    }

    @Override
    public final boolean addAll(int index, @NonNull Collection<? extends E> collection) {
        int oldSize = mArray.size();
        mArray.addAll(index, collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mData.notifyItemRangeInserted(index, count);
            return true;
        }
        return false;
    }

    @Override
    public E remove(int index) {
        E removed = mArray.remove(index);
        mData.notifyItemRemoved(index);
        return removed;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(@NonNull Object obj) {
        int index = mArray.indexOf(obj);
        if (index != -1) {
            mArray.remove(index);
            mData.notifyItemRemoved(index);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        int size = mArray.size();
        if (size > 0) {
            mArray.clear();
            mData.notifyItemRangeRemoved(0, size);
        }
    }

    void trimToSize() {
        mArray.trimToSize();
    }
}