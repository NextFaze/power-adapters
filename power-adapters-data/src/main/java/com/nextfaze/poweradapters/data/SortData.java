package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class SortData<T> extends DataWrapper<T> {

    @NonNull
    private final Data<? extends T> mData;

    @NonNull
    private final Comparator<? super T> mComparator;

    @NonNull
    private final Index mIndex = new Index();

    SortData(@NonNull Data<? extends T> data, @NonNull Comparator<? super T> comparator) {
        super(data);
        mData = checkNotNull(data, "data");
        mComparator = checkNotNull(comparator, "comparator");
    }

    @Override
    public int size() {
        return mIndex.size();
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        return mData.get(mIndex.outerToInner(position));
    }

    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        rebuild();
        int itemCount = mIndex.size();
        if (itemCount > 0) {
            notifyItemRangeInserted(0, itemCount);
        }
    }

    @Override
    protected void onLastDataObserverUnregistered() {
        super.onLastDataObserverUnregistered();
        mIndex.clear();
    }

    @Override
    protected void forwardChanged() {
        rebuild();
        notifyDataSetChanged();
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + innerItemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            int oldOuterPosition = mIndex.remove(innerPosition);
            notifyItemRemoved(oldOuterPosition);
            int newOuterPosition = findOuterPositionForValue(t);
            mIndex.insert(newOuterPosition, innerPosition);
            notifyItemInserted(newOuterPosition);
        }
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        mIndex.shift(innerPositionStart, +innerItemCount);
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + innerItemCount; innerPosition++) {
            T value = mData.get(innerPosition);
            int outerPosition = findOuterPositionForValue(value);
            mIndex.insert(outerPosition, innerPosition);
            notifyItemInserted(outerPosition);
        }
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + innerItemCount; innerPosition++) {
            notifyItemRemoved(mIndex.remove(innerPosition));
        }
        mIndex.shift(innerPositionStart, -innerItemCount);
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        // TODO: Support fine-grained notifications for moves.
        rebuild();
        notifyDataSetChanged();
    }

    private void rebuild() {
        mIndex.clear();
        int size = mData.size();
        for (int innerPosition = 0; innerPosition < size; innerPosition++) {
            T value = mData.get(innerPosition);
            int outerPosition = findOuterPositionForValue(value);
            mIndex.put(outerPosition, innerPosition);
        }
    }

    private int findOuterPositionForValue(T value) {
        final int size = mIndex.size();
        int lo = 0;
        int hi = size - 1;
        while (lo <= hi) {
            final int mid = (lo + hi) >>> 1;
            final T midVal = mData.get(mIndex.outerToInner(mid));
            int result = mComparator.compare(midVal, value);
            if (result < 0) {
                lo = mid + 1;
            } else if (result > 0) {
                hi = mid - 1;
            } else {
                return mid;
            }
        }
        return -(~lo) - 1;
    }

    private static final class Index {

        @NonNull
        private final ArrayList<Integer> mInnerPositions = new ArrayList<>();

        int size() {
            return mInnerPositions.size();
        }

        int outerToInner(int outerPosition) {
            return mInnerPositions.get(outerPosition);
        }

        void put(int outerPosition, int innerPosition) {
            mInnerPositions.add(outerPosition, innerPosition);
        }

        void insert(int outerPosition, int innerPosition) {
            mInnerPositions.add(outerPosition, innerPosition);
        }

        int remove(int innerPosition) {
            // TODO: Avoid O(N) search.
            int outerPosition = mInnerPositions.indexOf(innerPosition);
            mInnerPositions.remove(outerPosition);
            return outerPosition;
        }

        void shift(int innerPositionStart, int delta) {
            if (delta > 0) {
                for (int i = mInnerPositions.size() - 1; i >= 0; i--) {
                    int innerPosition = mInnerPositions.get(i);
                    if (innerPosition >= innerPositionStart) {
                        mInnerPositions.set(i, innerPosition + delta);
                    }
                }
            } else if (delta < 0) {
                for (int i = 0; i < mInnerPositions.size(); i++) {
                    int innerPosition = mInnerPositions.get(i);
                    if (innerPosition >= innerPositionStart) {
                        mInnerPositions.set(i, innerPosition + delta);
                    }
                }
            }
        }

        void clear() {
            mInnerPositions.clear();
        }
    }
}
