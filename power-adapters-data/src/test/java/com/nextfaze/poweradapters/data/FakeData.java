package com.nextfaze.poweradapters.data;

import android.support.annotation.Nullable;
import com.nextfaze.poweradapters.internal.NotificationType;
import com.nextfaze.poweradapters.internal.NotifyingArrayList;
import lombok.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import static java.util.Arrays.asList;

@SuppressWarnings("WeakerAccess")
public class FakeData<T> extends Data<T> implements List<T> {

    @NonNull
    private final NotifyingArrayList<T> mData = new NotifyingArrayList<>(mDataObservable);

    private int mAvailable = UNKNOWN;

    private boolean mLoading;

    @Override
    public int size() {
        return mData.size();
    }

    @Override
    public boolean contains(@Nullable Object object) {
        return mData.contains(object);
    }

    @Override
    public int indexOf(@Nullable Object object) {
        return mData.indexOf(object);
    }

    @Override
    public int lastIndexOf(@Nullable Object object) {
        return mData.lastIndexOf(object);
    }

    @Override
    public T remove(int index) {
        return mData.remove(index);
    }

    @Override
    public boolean add(@NonNull T t) {
        return mData.add(t);
    }

    @Override
    public void add(int index, T object) {
        mData.add(index, object);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> collection) {
        return mData.addAll(collection);
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> collection) {
        return mData.addAll(index, collection);
    }

    @Override
    public void clear() {
        mData.clear();
    }

    @Override
    public boolean remove(@NonNull Object obj) {
        return mData.remove(obj);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return mData.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int location) {
        return mData.listIterator(location);
    }

    @NonNull
    @Override
    public List<T> subList(int start, int end) {
        return mData.subList(start, end);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        return mData.containsAll(collection);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> collection) {
        return mData.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> collection) {
        return mData.retainAll(collection);
    }

    @Override
    public T set(int index, T object) {
        return mData.set(index, object);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return mData.toArray();
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    @NonNull
    @Override
    public <E> E[] toArray(@NonNull E[] contents) {
        return mData.toArray(contents);
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        //noinspection ConstantConditions
        return mData.get(position);
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

    @SuppressWarnings("unchecked")
    public void insert(int index, @NonNull T... items) {
        mData.addAll(index, asList(items));
    }

    @SuppressWarnings("unchecked")
    public void append(@NonNull T... items) {
        addAll(asList(items));
    }

    @SuppressWarnings("unchecked")
    public void change(int index, @NonNull T... items) {
        mData.setAll(index, asList(items));
    }

    public void remove(int index, int count) {
        mData.remove(index, count);
    }

    public void move(int fromPosition, int toPosition, int itemCount) {
        mData.move(fromPosition, toPosition, itemCount);
    }

    @SuppressWarnings("unused")
    @NonNull
    public NotificationType getNotificationType() {
        return mData.getNotificationType();
    }

    public void setNotificationType(@NonNull NotificationType notificationType) {
        mData.setNotificationType(notificationType);
    }
}
