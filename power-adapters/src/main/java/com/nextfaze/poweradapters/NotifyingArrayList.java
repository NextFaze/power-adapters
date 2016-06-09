package com.nextfaze.poweradapters;

import lombok.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;

/** @hide Not intended for public use. */
public final class NotifyingArrayList<E2> extends AbstractList<E2> {

    @NonNull
    private final PowerAdapter mParent;

    @NonNull
    private final ArrayList<E2> mArray = new ArrayList<>();

    public NotifyingArrayList(@NonNull PowerAdapter parent) {
        mParent = parent;
    }

    @Override
    public E2 get(int location) {
        return mArray.get(location);
    }

    @Override
    public int size() {
        return mArray.size();
    }

    @Override
    public E2 set(int index, @NonNull E2 object) {
        E2 e = mArray.set(index, object);
        mParent.notifyItemChanged(index);
        return e;
    }

    @Override
    public boolean add(@NonNull E2 e) {
        if (mArray.add(e)) {
            mParent.notifyItemInserted(mArray.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, @NonNull E2 object) {
        mArray.add(index, object);
        mParent.notifyItemInserted(index);
    }

    @Override
    public final boolean addAll(@NonNull Collection<? extends E2> collection) {
        int oldSize = mArray.size();
        mArray.addAll(collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mParent.notifyItemRangeInserted(oldSize, count);
            return true;
        }
        return false;
    }

    @Override
    public final boolean addAll(int index, @NonNull Collection<? extends E2> collection) {
        int oldSize = mArray.size();
        mArray.addAll(index, collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mParent.notifyItemRangeInserted(index, count);
            return true;
        }
        return false;
    }

    @Override
    public E2 remove(int index) {
        E2 removed = mArray.remove(index);
        mParent.notifyItemRemoved(index);
        return removed;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(@NonNull Object obj) {
        int index = mArray.indexOf(obj);
        if (index != -1) {
            mArray.remove(index);
            mParent.notifyItemRemoved(index);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        int size = mArray.size();
        if (size > 0) {
            mArray.clear();
            mParent.notifyItemRangeRemoved(0, size);
        }
    }
}
