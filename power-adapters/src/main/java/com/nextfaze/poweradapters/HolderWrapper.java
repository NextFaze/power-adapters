package com.nextfaze.poweradapters;

import android.support.annotation.NonNull;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class HolderWrapper implements Holder {

    @NonNull
    private final Holder mHolder;

    public HolderWrapper(@NonNull Holder holder) {
        mHolder = checkNotNull(holder, "holder");
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
