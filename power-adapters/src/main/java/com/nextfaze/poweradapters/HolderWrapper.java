package com.nextfaze.poweradapters;

import lombok.NonNull;

public class HolderWrapper implements Holder {

    @NonNull
    private final Holder mHolder;

    public HolderWrapper(@NonNull Holder holder) {
        mHolder = holder;
    }

    @NonNull
    public final Holder getHolder() {
        return mHolder;
    }

    @Override
    public int getPosition() {
        return mHolder.getPosition();
    }
}
