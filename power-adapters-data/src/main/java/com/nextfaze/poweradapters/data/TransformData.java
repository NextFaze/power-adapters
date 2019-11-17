package com.nextfaze.poweradapters.data;

import androidx.annotation.NonNull;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class TransformData<F, T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends F> mData;

    @NonNull
    private final Function<? super F, ? extends T> mFunction;

    TransformData(@NonNull Data<? extends F> data, @NonNull Function<? super F, ? extends T> function) {
        super(data);
        mData = checkNotNull(data, "data");
        mFunction = checkNotNull(function, "function");
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mFunction.apply(mData.get(position, flags));
    }
}
