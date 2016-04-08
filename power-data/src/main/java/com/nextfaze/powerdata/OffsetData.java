package com.nextfaze.powerdata;

import lombok.NonNull;

final class OffsetData<T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends T> mData;

    private final int mOffset;

    public OffsetData(@NonNull Data<? extends T> data, int offset) {
        super(data);
        mData = data;
        mOffset = offset;
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mData.get(position, flags);
    }

    // TODO: Offset notifications.

    // TODO: Override size() to reduce total, subtracting offset.
}
