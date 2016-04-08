package com.nextfaze.powerdata;

import lombok.NonNull;

import java.util.Iterator;

final class DataIterator<T> implements Iterator<T> {

    @NonNull
    private final Data<? extends T> mData;

    private int mPosition;

    DataIterator(@NonNull Data<? extends T> data) {
        mData = data;
    }

    @Override
    public boolean hasNext() {
        return mPosition < mData.size();
    }

    @Override
    public T next() {
        return mData.get(mPosition++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
