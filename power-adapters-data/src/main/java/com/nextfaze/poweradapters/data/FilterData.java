package com.nextfaze.poweradapters.data;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.Predicate;

import java.util.ArrayList;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
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
        mData = checkNotNull(data, "data");
        mPredicate = checkNotNull(predicate, "predicate");
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
        return mIndex.size();
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        if (position < 0 || position >= mIndex.size()) {
            throw new IndexOutOfBoundsException(format("Position %s, size %s", position, mIndex.size()));
        }
        int innerSize = mData.size();
        int innerPosition = mIndex.outerToInner(position);
        if (innerPosition < 0 || innerPosition >= innerSize) {
            // Throw a useful error message if a bug results in an index inconsistency.
            throw new AssertionError(
                    format("Index inconsistency: index entry at position %s points to inner position %s, but inner data size is %s. The inner data content may have changed without a corresponding change notification.",
                            position, innerPosition, innerSize));
        }
        return mData.get(innerPosition, flags);
    }

    @Override
    protected void onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered();
        rebuild();
        int itemCount = mIndex.size();
        if (itemCount > 0) {
            notifyItemRangeChanged(0, itemCount);
        }
    }

    @Override
    protected void onLastDataObserverUnregistered() {
        super.onLastDataObserverUnregistered();
        mIndex.clear();
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
                                  final boolean notifyChanges,
                                  final boolean notifyInsertions,
                                  final boolean notifyRemovals) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            boolean include = apply(t);
            // Search for existing mapping.
            int i = mIndex.binarySearch(innerPosition);
            if (i >= 0) {
                // Mapping already exists.
                if (include) {
                    // Item should be included. Notify of a change.
                    if (notifyChanges) {
                        notifyItemChanged(i);
                    }
                } else {
                    // Item shouldn't be included. Remove mapping and notify of removal.
                    mIndex.remove(i);
                    if (notifyRemovals) {
                        notifyItemRemoved(i);
                    }
                }
            } else {
                // No mapping exists.
                if (include) {
                    // Item should be included. Insert new mapping and notify of insertion.
                    // Use binary search result value to find out what the notification position should be.
                    int insertionOuterPosition = ~i;
                    mIndex.add(insertionOuterPosition, innerPosition);
                    if (notifyInsertions) {
                        notifyItemInserted(insertionOuterPosition);
                    }
                }
            }
        }
    }

    private void insertIndexRange(final int innerPositionStart, final int itemCount) {
        int i = mIndex.binarySearch(innerPositionStart);
        // Use binary search result value to find out what the mapping should be.
        int insertionOuterPosition = i >= 0 ? i : ~i;
        mIndex.shift(insertionOuterPosition, +itemCount);
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            if (apply(t)) {
                mIndex.add(insertionOuterPosition, innerPosition);
                notifyItemInserted(insertionOuterPosition);
                insertionOuterPosition++;
            }
        }
    }

    private void removeIndexRange(final int innerPositionStart, final int itemCount) {
        for (int innerPosition = innerPositionStart + itemCount - 1;
             innerPosition >= innerPositionStart; innerPosition--) {
            int i = mIndex.binarySearch(innerPosition);
            if (i >= 0) {
                mIndex.remove(i);
                mIndex.shift(i, -1);
                notifyItemRemoved(i);
            } else {
                mIndex.shift(~i, -1);
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

        /** Sorted list of inner data positions. */
        @NonNull
        private final ArrayList<Integer> mArray = new ArrayList<>();

        void clear() {
            mArray.clear();
        }

        int size() {
            return mArray.size();
        }

        void remove(int outerPosition) {
            mArray.remove(outerPosition);
        }

        int outerToInner(int outerPosition) {
            return mArray.get(outerPosition);
        }

        void add(int outerPosition, int innerPosition) {
            mArray.add(outerPosition, innerPosition);
        }

        void shift(int outerPositionStart, int delta) {
            for (int i = outerPositionStart; i < mArray.size(); i++) {
                mArray.set(i, mArray.get(i) + delta);
            }
        }

        int binarySearch(int innerPosition) {
            int lo = 0;
            int hi = mArray.size() - 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                int midVal = mArray.get(mid);
                if (midVal < innerPosition) {
                    lo = mid + 1;
                } else if (midVal > innerPosition) {
                    hi = mid - 1;
                } else {
                    return mid;
                }
            }
            return ~lo;
        }
    }
}
