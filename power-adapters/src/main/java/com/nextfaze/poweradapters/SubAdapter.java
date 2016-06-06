package com.nextfaze.poweradapters;

import lombok.NonNull;

class SubAdapter extends PowerAdapterWrapper {

    private int mOffset;

    SubAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
    }

    int getOffset() {
        return mOffset;
    }

    void setOffset(int offset) {
        mOffset = offset;
    }

    @Override
    protected int outerToInner(int outerPosition) {
        return outerPosition - mOffset;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        return mOffset + innerPosition;
    }
}
