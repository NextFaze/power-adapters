package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.nextfaze.poweradapters.internal.NotificationType;
import com.nextfaze.poweradapters.internal.NotifyingArrayList;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("WeakerAccess")
@VisibleForTesting
public class FakeData<T> extends Data<T> {

    @NonNull
    private final NotifyingArrayList<T> mData = new NotifyingArrayList<>(mDataObservable);

    private int mAvailable = UNKNOWN;

    private boolean mLoading;

    @Override
    public int size() {
        return mData.size();
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        //noinspection ConstantConditions
        return mData.get(position);
    }

    public boolean add(@NonNull T t) {
        return mData.add(t);
    }

    public boolean addAll(@NonNull Collection<? extends T> collection) {
        return mData.addAll(collection);
    }

    public boolean addAll(int index, @NonNull Collection<? extends T> collection) {
        return mData.addAll(index, collection);
    }

    public void clear() {
        mData.clear();
    }

    public boolean remove(@NonNull T t) {
        return mData.remove(t);
    }

    public T set(int index, T object) {
        return mData.set(index, object);
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

    @NonNull
    @Override
    public List<T> asList() {
        return mData;
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
        mData.addAll(index, Arrays.asList(items));
    }

    @SuppressWarnings("unchecked")
    public void append(@NonNull T... items) {
        addAll(Arrays.asList(items));
    }

    @SuppressWarnings("unchecked")
    public void change(int index, @NonNull T... items) {
        mData.setAll(index, Arrays.asList(items));
    }

    public void remove(int index, int count) {
        mData.remove(index, count);
    }

    public void move(int fromPosition, int toPosition, int itemCount) {
        mData.move(fromPosition, toPosition, itemCount);
    }

    public void error(@NonNull Throwable e) {
        super.notifyError(e);
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
