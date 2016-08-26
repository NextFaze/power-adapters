package com.nextfaze.poweradapters;

import android.view.View;
import lombok.NonNull;

import java.util.WeakHashMap;

class SubAdapter extends PowerAdapterWrapper {

    @NonNull
    private final WeakHashMap<Holder, HolderWrapperImpl> mHolders = new WeakHashMap<>();

    @NonNull
    private final WeakHashMap<Container, ContainerWrapperImpl> mContainers = new WeakHashMap<>();

    int mOffset;

    @NonNull
    final Transform mInnerToOuterTransform;

    @NonNull
    final Transform mOuterToInnerTransform;

    SubAdapter(@NonNull PowerAdapter adapter) {
        super(adapter);
        mInnerToOuterTransform = new Transform() {
            @Override
            public int apply(int position) {
                return innerToOuter(position);
            }
        };
        mOuterToInnerTransform = new Transform() {
            @Override
            public int apply(int position) {
                return outerToInner(position);
            }
        };
    }

    SubAdapter(@NonNull PowerAdapter adapter,
               @NonNull Transform innerToOuterTransform,
               @NonNull Transform outerToInnerTransform) {
        super(adapter);
        mInnerToOuterTransform = innerToOuterTransform;
        mOuterToInnerTransform = outerToInnerTransform;
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
    public void bindView(@NonNull Container container, @NonNull View view, @NonNull Holder holder) {
        HolderWrapperImpl holderWrapper = mHolders.get(holder);
        if (holderWrapper == null) {
            holderWrapper = new HolderWrapperImpl(holder);
            mHolders.put(holder, holderWrapper);
        }
        ContainerWrapperImpl containerWrapper = mContainers.get(container);
        if (containerWrapper == null) {
            containerWrapper = new ContainerWrapperImpl(container);
            mContainers.put(container, containerWrapper);
        }
        getAdapter().bindView(containerWrapper, view, holderWrapper);
    }

    private final class HolderWrapperImpl extends HolderWrapper {

        HolderWrapperImpl(@NonNull Holder holder) {
            super(holder);
        }

        @Override
        public int getPosition() {
            return mOuterToInnerTransform.apply(super.getPosition());
        }
    }

    private final class ContainerWrapperImpl extends ContainerWrapper {

        ContainerWrapperImpl(@NonNull Container container) {
            super(container);
        }

        @Override
        public void scrollToPosition(int position) {
            super.scrollToPosition(mInnerToOuterTransform.apply(position));
        }

        @Override
        public int getItemCount() {
            return getAdapter().getItemCount();
        }
    }

    interface Transform {
        int apply(int position);
    }
}
