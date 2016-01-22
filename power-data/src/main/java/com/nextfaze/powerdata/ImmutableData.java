package com.nextfaze.powerdata;

import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.addAll;
import static java.util.Collections.emptyList;

@Accessors(prefix = "m")
public final class ImmutableData<T> extends AbstractData<T> {

    private static final ImmutableData<Object> EMPTY = new ImmutableData<>(emptyList());

    @NonNull
    private final List<? extends T> mElements;

    @SafeVarargs
    @NonNull
    public static <T> ImmutableData<T> of(@NonNull T... elements) {
        if (elements.length <= 0) {
            return emptyData();
        }
        ArrayList<T> list = new ArrayList<>(elements.length);
        addAll(list, elements);
        return new ImmutableData<>(list);
    }

    @NonNull
    public static <T> ImmutableData<T> of(@NonNull Iterable<? extends T> elements) {
        ArrayList<T> list = new ArrayList<>();
        for (T t : elements) {
            list.add(t);
        }
        if (list.isEmpty()) {
            return emptyData();
        }
        return new ImmutableData<>(list);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private static <T> ImmutableData<T> emptyData() {
        return (ImmutableData<T>) EMPTY;
    }

    ImmutableData(@NonNull List<? extends T> elements) {
        mElements = elements;
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mElements.get(position);
    }

    @Override
    public int size() {
        return mElements.size();
    }

    @Override
    public boolean isLoading() {
        return false;
    }

    @Override
    public int available() {
        return 0;
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
}
