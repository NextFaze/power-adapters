package com.nextfaze.poweradapters.binding;

import lombok.NonNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A mutable {@link BindingAdapter} implementation that automatically generates notifications when its contents
 * changes. This class does not accept {@code null} elements.
 */
public final class ListBindingAdapter<E> extends BindingAdapter implements List<E> {

    @NonNull
    private final NotifyingArrayList<E> mData = new NotifyingArrayList<>();

    public ListBindingAdapter(@NonNull Mapper mapper, @NonNull E... items) {
        super(mapper);
        Collections.addAll(mData, items);
    }

    public ListBindingAdapter(@NonNull Mapper mapper, @NonNull List<? extends E> list) {
        super(mapper);
        mData.addAll(list);
    }

    @NonNull
    @Override
    protected Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void add(int location, @NonNull E object) {
        mData.add(location, object);
    }

    @Override
    public boolean add(@NonNull E object) {
        return mData.add(object);
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends E> collection) {
        return mData.addAll(location, collection);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        return mData.addAll(collection);
    }

    @Override
    public void clear() {
        mData.clear();
    }

    @Override
    public boolean contains(Object object) {
        return mData.contains(object);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return mData.containsAll(collection);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object object) {
        return mData.equals(object);
    }

    @Override
    public E get(int location) {
        return mData.get(location);
    }

    @Override
    public int hashCode() {
        return mData.hashCode();
    }

    @Override
    public int indexOf(Object object) {
        return mData.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return mData.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<E> iterator() {
        return mData.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return mData.lastIndexOf(object);
    }

    @Override
    public ListIterator<E> listIterator() {
        return mData.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<E> listIterator(int location) {
        return mData.listIterator(location);
    }

    @Override
    public E remove(int location) {
        return mData.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return mData.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return mData.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return mData.retainAll(collection);
    }

    @Override
    public E set(int location, E object) {
        return mData.set(location, object);
    }

    @Override
    public int size() {
        return mData.size();
    }

    @NonNull
    @Override
    public List<E> subList(int start, int end) {
        return mData.subList(start, end);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mData.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        return mData.toArray(array);
    }

    private final class NotifyingArrayList<E2> extends AbstractList<E2> {

        @NonNull
        private final ArrayList<E2> mArray = new ArrayList<>();

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
            notifyItemChanged(index);
            return e;
        }

        @Override
        public boolean add(@NonNull E2 e) {
            if (mArray.add(e)) {
                notifyItemInserted(mArray.size() - 1);
                return true;
            }
            return false;
        }

        @Override
        public void add(int index, @NonNull E2 object) {
            mArray.add(index, object);
            notifyItemInserted(index);
        }

        @Override
        public final boolean addAll(@NonNull Collection<? extends E2> collection) {
            int oldSize = mArray.size();
            mArray.addAll(collection);
            int newSize = mArray.size();
            if (newSize != oldSize) {
                int count = mArray.size() - oldSize;
                notifyItemRangeInserted(oldSize, count);
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
                notifyItemRangeInserted(index, count);
                return true;
            }
            return false;
        }

        @Override
        public E2 remove(int index) {
            E2 removed = mArray.remove(index);
            notifyItemRemoved(index);
            return removed;
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public boolean remove(@NonNull Object obj) {
            int index = mArray.indexOf(obj);
            if (index != -1) {
                mArray.remove(index);
                notifyItemRemoved(index);
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            int size = mArray.size();
            if (size > 0) {
                mArray.clear();
                notifyItemRangeRemoved(0, size);
            }
        }
    }
}
