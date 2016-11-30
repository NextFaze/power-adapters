package com.nextfaze.poweradapters.internal;

import lombok.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.lang.Math.min;
import static java.util.Collections.swap;

/** @hide Not intended for public use. */
public final class NotifyingArrayList<E> extends AbstractList<E> {

    @NonNull
    private final DataObservable mDataObservable;

    @NonNull
    private final ArrayList<E> mArray = new ArrayList<>();

    @NonNull
    private NotificationType mNotificationType = NotificationType.FINE;

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
        mNotificationType.notifyItemChanged(mDataObservable, index);
        return e;
    }

    @Override
    public boolean add(@NonNull E e) {
        if (mArray.add(e)) {
            mNotificationType.notifyItemInserted(mDataObservable, mArray.size() - 1);
            return true;
        }
        return false;
    }

    @Override
    public void add(int index, @NonNull E object) {
        mArray.add(index, object);
        mNotificationType.notifyItemInserted(mDataObservable, index);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        int oldSize = mArray.size();
        mArray.addAll(collection);
        int newSize = mArray.size();
        if (newSize != oldSize) {
            int count = mArray.size() - oldSize;
            mNotificationType.notifyItemRangeInserted(mDataObservable, oldSize, count);
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
            mNotificationType.notifyItemRangeInserted(mDataObservable, index, count);
            return true;
        }
        return false;
    }

    @Override
    public E remove(int index) {
        E removed = mArray.remove(index);
        mNotificationType.notifyItemRemoved(mDataObservable, index);
        return removed;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(@NonNull Object obj) {
        int index = mArray.indexOf(obj);
        if (index != -1) {
            mArray.remove(index);
            mNotificationType.notifyItemRemoved(mDataObservable, index);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        int size = mArray.size();
        if (size > 0) {
            mArray.clear();
            mNotificationType.notifyItemRangeRemoved(mDataObservable, 0, size);
        }
    }

    public void replaceAll(@NonNull List<? extends E> collection) {
        int oldSize = mArray.size();

        // Add all non-null elements
        mArray.clear();
        for (E e : collection) {
            if (e != null) {
                mArray.add(e);
            }
        }

        // Issue removal/insertion notifications. These must happen first, otherwise downstream item count
        // verification will complain that our size has changed without a corresponding structural notification.
        int deltaSize = mArray.size() - oldSize;
        if (deltaSize < 0) {
            mNotificationType.notifyItemRangeRemoved(mDataObservable, oldSize + deltaSize, -deltaSize);
        } else if (deltaSize > 0) {
            mNotificationType.notifyItemRangeInserted(mDataObservable, oldSize, deltaSize);
        }

        // Finally, issue a change notification for the range of elements not accounted for above.
        mNotificationType.notifyItemRangeChanged(mDataObservable, 0, min(oldSize, mArray.size()));
    }

    public void setAll(int index, @NonNull Collection<? extends E> collection) {
        int i = 0;
        for (E e : collection) {
            mArray.set(index + i, e);
            i++;
        }
        mNotificationType.notifyItemRangeChanged(mDataObservable, index, collection.size());
    }

    public void remove(int index, int count) {
        for (int i = 0; i < count; i++) {
            mArray.remove(index);
        }
        mNotificationType.notifyItemRangeRemoved(mDataObservable, index, count);
    }

    public void move(int fromPosition, int toPosition, int itemCount) {
        if (itemCount <= 0) {
            throw new IllegalArgumentException("count <= 0");
        }
        if (fromPosition < toPosition) {
            for (int j = itemCount - 1; j >= 0; j--) {
                for (int i = fromPosition + j; i < toPosition + j; i++) {
                    swap(mArray, i, i + 1);
                }
            }
        } else {
            for (int j = 0; j < itemCount; j++) {
                for (int i = fromPosition + j; i > toPosition + j; i--) {
                    swap(mArray, i, i - 1);
                }
            }
        }
        mNotificationType.notifyItemRangeMoved(mDataObservable, fromPosition, toPosition, itemCount);
    }

    public void trimToSize() {
        mArray.trimToSize();
    }

    public void ensureCapacity(int minimumCapacity) {
        mArray.ensureCapacity(minimumCapacity);
    }

    @NonNull
    public NotificationType getNotificationType() {
        return mNotificationType;
    }

    public void setNotificationType(@NonNull NotificationType notificationType) {
        mNotificationType = notificationType;
    }
}
