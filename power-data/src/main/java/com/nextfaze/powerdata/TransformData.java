package com.nextfaze.powerdata;

import lombok.NonNull;

final class TransformData<F, T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends F> mData;

    @NonNull
    private final Function<? super F, ? extends T> mFunction;

    TransformData(@NonNull Data<? extends F> data, @NonNull Function<? super F, ? extends T> function) {
        super(data);
        mData = data;
        mFunction = function;
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mFunction.apply(mData.get(position, flags));
    }
}
