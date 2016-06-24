package com.nextfaze.poweradapters.data.rx;

public final class MoveEvent {

    private final int mFromPosition;

    private final int mToPosition;

    private final int mCount;

    public MoveEvent(int fromPosition, int toPosition, int count) {
        mFromPosition = fromPosition;
        mToPosition = toPosition;
        mCount = count;
    }

    public int getFromPosition() {
        return mFromPosition;
    }

    public int getToPosition() {
        return mToPosition;
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
        MoveEvent moveEvent = (MoveEvent) o;
        return mFromPosition == moveEvent.mFromPosition &&
                mToPosition == moveEvent.mToPosition && mCount == moveEvent.mCount;

    }

    @Override
    public int hashCode() {
        int result = mFromPosition;
        result = 31 * result + mToPosition;
        result = 31 * result + mCount;
        return result;
    }
}
