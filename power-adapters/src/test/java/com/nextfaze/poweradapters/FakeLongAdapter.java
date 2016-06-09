package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** An adapter used for testing that has stable IDs. */
public class FakeLongAdapter extends PowerAdapter implements List<Long> {

    @NonNull
    private final NotifyingArrayList<Long> mData = new NotifyingArrayList<>(this);

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        return mViewType;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        return new View(parent.getContext());
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void add(int location, @NonNull Long object) {
        mData.add(location, object);
    }

    @Override
    public boolean add(@NonNull Long object) {
        return mData.add(object);
    }

    @Override
    public boolean addAll(int location, @NonNull Collection<? extends Long> collection) {
        return mData.addAll(location, collection);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends Long> collection) {
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
    public Long get(int location) {
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
    public Iterator<Long> iterator() {
        return mData.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return mData.lastIndexOf(object);
    }

    @Override
    public ListIterator<Long> listIterator() {
        return mData.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<Long> listIterator(int location) {
        return mData.listIterator(location);
    }

    @Override
    public Long remove(int location) {
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
    public Long set(int location, Long object) {
        return mData.set(location, object);
    }

    @Override
    public int size() {
        return mData.size();
    }

    @NonNull
    @Override
    public List<Long> subList(int start, int end) {
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
}
