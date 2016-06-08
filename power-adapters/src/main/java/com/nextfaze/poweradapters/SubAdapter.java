package com.nextfaze.poweradapters;

import android.view.View;
import lombok.NonNull;

import java.util.WeakHashMap;

class SubAdapter extends PowerAdapterWrapper {

    @NonNull
    private final WeakHashMap<Holder, HolderWrapperImpl> mHolders = new WeakHashMap<>();

    private int mOffset;

    @NonNull
    private final HolderTransform mHolderTransform;

    SubAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
        mHolderTransform = new HolderTransform() {
            @Override
            public int apply(int outerPosition) {
                return outerToInner(outerPosition);
            }
        };
    }

    SubAdapter(@NonNull PowerAdapter adapter, @NonNull HolderTransform holderTransform) {
        super(adapter);
        mHolderTransform = holderTransform;
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

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        HolderWrapperImpl holderWrapper = mHolders.get(holder);
        if (holderWrapper == null) {
            holderWrapper = new HolderWrapperImpl(holder);
            mHolders.put(holder, holderWrapper);
        }
        getAdapter().bindView(view, holderWrapper);
    }

    private final class HolderWrapperImpl extends HolderWrapper {

        HolderWrapperImpl(@NonNull Holder holder) {
            super(holder);
        }

        @Override
        public int getPosition() {
            return mHolderTransform.apply(super.getPosition());
        }
    }

    interface HolderTransform {
        int apply(int outerPosition);
    }
}
