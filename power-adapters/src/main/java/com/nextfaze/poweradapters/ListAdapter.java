package com.nextfaze.poweradapters;

import lombok.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A mutable adapter that automatically generates notifications when its contents changes.
 * This class does not accept {@code null} elements. Binding of elements to views is left up to the subclass.
 */
public abstract class ListAdapter<E> extends PowerAdapter implements List<E> {

    @NonNull
    private final NotifyingArrayList<E> mData = new NotifyingArrayList<>(mDataObservable);

    @SuppressWarnings("WeakerAccess")
    public ListAdapter() {
    }

    public ListAdapter(@NonNull Collection<? extends E> list) {
        mData.addAll(list);
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
}
