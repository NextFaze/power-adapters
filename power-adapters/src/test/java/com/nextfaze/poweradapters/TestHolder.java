package com.nextfaze.poweradapters;

final class TestHolder implements Holder {

    private int mPosition;

    TestHolder() {
    }

    TestHolder(int position) {
        this.mPosition = position;
    }

    void setPosition(int position) {
        mPosition = position;
    }

    @Override
    public int getPosition() {
        return mPosition;
    }
}
