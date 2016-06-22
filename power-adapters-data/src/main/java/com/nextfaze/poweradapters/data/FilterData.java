package com.nextfaze.poweradapters.data;

import android.util.SparseIntArray;
import com.nextfaze.poweradapters.Predicate;
import lombok.NonNull;

import static java.lang.String.format;

/** Provides a filtered view of the wrapped data. */
public final class FilterData<T> extends DataWrapper<T> {

    private static final Predicate<Object> ALWAYS = new Predicate<Object>() {
        @Override
        public boolean apply(Object o) {
            return true;
        }
    };

    @NonNull
    private final Data<? extends T> mData;

    @NonNull
    private Predicate<? super T> mPredicate;

    @NonNull
    private final Index mIndex = new Index();

    public FilterData(@NonNull Data<? extends T> data) {
        this(data, ALWAYS);
    }

    public FilterData(@NonNull Data<? extends T> data, @NonNull Predicate<? super T> predicate) {
        super(data);
        mData = data;
        mPredicate = predicate;
    }

    @NonNull
    public Predicate<? super T> getPredicate() {
        return mPredicate;
    }

    public void setPredicate(@NonNull Predicate<? super T> predicate) {
        if (!equal(predicate, mPredicate)) {
            mPredicate = predicate;
            changeIndexRange(0, mData.size(), false, true, true);
        }
    }

    @Override
    public int size() {
        if (getDataObserverCount() <= 0) {
            return calculateSize();
        }
        return mIndex.size();
    }

    private int calculateSize() {
        int totalSize = 0;
        for (int i = 0; i < mData.size(); i++) {
            if (apply(mData.get(i))) {
                totalSize++;
            }
        }
        return totalSize;
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        if (position < 0 || position >= mIndex.size()) {
            throw new IndexOutOfBoundsException(format("Position %s, size %s", position, mIndex.size()));
        }
        int innerPosition = mIndex.outerToInner(position);
        if (innerPosition < 0 || innerPosition >= mData.size()) {
            // Throw a useful error message if a bug results in an index inconsistency.
            throw new AssertionError(
                    format("Index inconsistency: index entry at position %s points to inner position %s, but inner data size is %s. The inner data content may have changed without a corresponding change notification.",
                            position, innerPosition, mData.size()));
        }
        return mData.get(innerPosition, flags);
    }

    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        rebuild();
    }

    private void rebuild() {
        changeIndexRange(0, mData.size(), false, false, false);
    }

    @Override
    protected void forwardChanged() {
        changeIndexRange(0, mData.size(), true, true, true);
    }

    @Override
    protected void forwardItemRangeChanged(int innerPositionStart, int innerItemCount) {
        changeIndexRange(innerPositionStart, innerItemCount, true, true, true);
    }

    @Override
    protected void forwardItemRangeInserted(int innerPositionStart, int innerItemCount) {
        insertIndexRange(innerPositionStart, innerItemCount);
    }

    @Override
    protected void forwardItemRangeRemoved(int innerPositionStart, int innerItemCount) {
        removeIndexRange(innerPositionStart, innerItemCount);
    }

    @Override
    protected void forwardItemRangeMoved(int innerFromPosition, int innerToPosition, int innerItemCount) {
        // TODO: Fine-grained notifications for moves.
        changeIndexRange(0, mData.size(), false, false, false);
        notifyDataSetChanged();
    }

    private void changeIndexRange(final int innerPositionStart,
                                  final int itemCount,
                                  boolean notifyChanges,
                                  boolean notifyInsertions,
                                  boolean notifyRemovals) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            boolean include = apply(t);
            // Check for existing mapping.
            int outerPosition = mIndex.innerToOuter(innerPosition);
            if (outerPosition >= 0) {
                // Mapping already exists.
                if (include) {
                    // Item should be included. Overwrite mapping and notify of a change.
                    mIndex.put(innerPosition);
                    if (notifyChanges) {
                        notifyItemChanged(outerPosition);
                    }
                } else {
                    // Item shouldn't be included. Remove mapping and notify of removal.
                    mIndex.remove(outerPosition);
                    if (notifyRemovals) {
                        notifyItemRemoved(outerPosition);
                    }
                }
            } else {
                // No mapping exists.
                if (include) {
                    // Item should be included. Insert new mapping and notify of insertion.
                    // Take advantage of indexOfKey() binary search result value to find out what the mapping should be.
                    int insertionIndex = outerPosition >= 0 ? outerPosition : -outerPosition - 1;
                    mIndex.put(innerPosition);
                    if (notifyInsertions) {
                        notifyItemInserted(insertionIndex);
                    }
                }
            }
        }
    }

    private void insertIndexRange(final int innerPositionStart, final int itemCount) {
        // Take advantage of indexOfKey() binary search result value to find out what the mapping should be.
        int idx = mIndex.innerToOuter(innerPositionStart);
        int insertionIndex = idx >= 0 ? idx : -idx - 1;
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            if (apply(t)) {
                mIndex.put(innerPosition);
                notifyItemInserted(insertionIndex);
                insertionIndex++;
            }
        }
    }

    private void removeIndexRange(final int innerPositionStart, final int itemCount) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            int outerPosition = mIndex.innerToOuter(innerPosition);
            if (outerPosition >= 0) {
                mIndex.remove(outerPosition);
                notifyItemRemoved(outerPosition);
            }
        }
    }

    private boolean apply(@NonNull T t) {
        return mPredicate.apply(t);
    }

    private static boolean equal(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

    private static final class Index {

        @NonNull
        private final SparseIntArray mArray = new SparseIntArray();

        int size() {
            return mArray.size();
        }

        void put(int innerPosition) {
            mArray.put(innerPosition, 0);
        }

        void remove(int outerPosition) {
            mArray.removeAt(outerPosition);
        }

        int innerToOuter(int innerPosition) {
            return mArray.indexOfKey(innerPosition);
        }

        int outerToInner(int outerPosition) {
            return mArray.keyAt(outerPosition);
        }
    }
}
