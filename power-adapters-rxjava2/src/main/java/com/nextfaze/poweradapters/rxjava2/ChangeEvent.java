package com.nextfaze.poweradapters.rxjava2;

import android.support.annotation.Nullable;

public final class ChangeEvent {

    private final int mPosition;

    private final int mCount;

    @Nullable
    private final Object mPayload;

    public ChangeEvent(int position, int count, @Nullable Object payload) {
        mPosition = position;
        mCount = count;
        mPayload = payload;
    }

    public int getPosition() {
        return mPosition;
    }

    public int getCount() {
        return mCount;
    }

    @Nullable
    public Object getPayload() {
        return mPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChangeEvent changeEvent = (ChangeEvent) o;
        return mPosition == changeEvent.mPosition && mCount == changeEvent.mCount;
    }

    @Override
    public int hashCode() {
        int result = mPosition;
        result = 31 * result + mCount;
        return result;
    }
}
