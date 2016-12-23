package com.nextfaze.poweradapters.rx;

public final class ChangeEvent {

    private final int mPosition;

    private final int mCount;

    public ChangeEvent(int position, int count) {
        mPosition = position;
        mCount = count;
    }

    public int getPosition() {
        return mPosition;
    }

    public int getCount() {
        return mCount;
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
