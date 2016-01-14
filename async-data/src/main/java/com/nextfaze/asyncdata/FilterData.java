package com.nextfaze.asyncdata;

import android.os.Handler;
import android.util.SparseIntArray;
import com.android.internal.util.Predicate;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static android.os.Looper.getMainLooper;
import static java.lang.Math.abs;
import static java.lang.String.format;

/** Maintains an index into the wrapped data instance. */
@Slf4j
final class FilterData<T> extends AbstractData<T> {

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final Data<? extends T> mData;

    @NonNull
    private final Predicate<? super T> mPredicate;

    @NonNull
    private final Index mIndex = new Index();

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChange() {
            changeIndexRange(0, mData.size(), true);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            changeIndexRange(positionStart, itemCount, true);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            insertIndexRange(positionStart, itemCount, true);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            removeIndexRange(positionStart, itemCount, true);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            moveIndexRange(fromPosition, toPosition, itemCount, true);
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            updateLoading();
        }
    };

    @NonNull
    private final AvailableObserver mAvailableObserver = new AvailableObserver() {
        @Override
        public void onAvailableChange() {
            updateAvailable();
        }
    };

    @NonNull
    private final ErrorObserver mErrorObserver = new ErrorObserver() {
        @Override
        public void onError(@NonNull Throwable e) {
            notifyError(e);
        }
    };

    private boolean mObservingData;
    private boolean mObservingLoading;
    private boolean mObservingError;
    private boolean mObservingAvailable;

    private boolean mLoading;
    private int mAvailable = UNKNOWN;

    private boolean mEntireIndexDirty = true;

    FilterData(@NonNull Data<? extends T> data, @NonNull Predicate<? super T> predicate) {
        mData = data;
        mPredicate = predicate;
    }

    @Override
    public void close() {
        unregisterAll();
        super.close();
    }

    @NonNull
    @Override
    public T get(int position, int flags) {
        assertObservingData();
        rebuildIndexIfNeeded();
        if (position < 0 || position >= mIndex.size()) {
            throw new IndexOutOfBoundsException(format("Position %s, size %s", position, mIndex.size()));
        }
        Integer innerPosition = mIndex.keyAt(position);
        if (innerPosition < 0 || innerPosition >= mData.size()) {
            // Throw a useful error message if a bug results in an index inconsistency.
            throw new AssertionError(
                    format("Index inconsistency: index entry at position %s points to inner position %s, but inner data size is %s. The inner data content may have changed without a corresponding change notification.",
                            position, innerPosition, mData.size()));
        }
        return mData.get(innerPosition, flags);
    }

    private void assertObservingData() {
        // It's incorrect to access the elements of this Data without being registered as a data observer.
        // We maintain an index into the inner wrapped Data, and therefore we need to be notified when the wrapped
        // data changes so we can update the index.
        // In order to register with the wrapped data in a way that doesn't leak, we rely on counting the
        // registered observers. Thus, clients of this class MUST be registered observers.
        if (!mObservingData) {
            throw new IllegalStateException("Not registered with inner data");
        }
    }

    @Override
    public int size() {
        rebuildIndexIfNeeded();
        return mIndex.size();
    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public int available() {
        return mAvailable;
    }

    @Override
    public void invalidate() {
        mData.invalidate();
    }

    @Override
    public void refresh() {
        mData.refresh();
    }

    @Override
    public void reload() {
        mData.reload();
    }

    @Override
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        super.registerDataObserver(dataObserver);
        updateDataObserver();
    }

    @Override
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        super.unregisterDataObserver(dataObserver);
        updateDataObserver();
    }

    @Override
    public void registerAvailableObserver(@NonNull AvailableObserver availableObserver) {
        super.registerAvailableObserver(availableObserver);
        updateAvailableObserver();
    }

    @Override
    public void unregisterAvailableObserver(@NonNull AvailableObserver availableObserver) {
        super.unregisterAvailableObserver(availableObserver);
        updateAvailableObserver();
    }

    @Override
    public void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        super.registerLoadingObserver(loadingObserver);
        updateLoadingObserver();
    }

    @Override
    public void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        super.unregisterLoadingObserver(loadingObserver);
        updateLoadingObserver();
    }

    @Override
    public void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        super.registerErrorObserver(errorObserver);
        updateErrorObserver();
    }

    @Override
    public void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        super.unregisterErrorObserver(errorObserver);
        updateErrorObserver();
    }

    private void invalidateEntireIndex() {
        mEntireIndexDirty = true;
        rebuildIndexIfNeeded();
    }

    private void rebuildIndexIfNeeded() {
        if (mEntireIndexDirty) {
            mEntireIndexDirty = false;
            buildCompleteIndex();
        }
    }

    private void updateLoading() {
        boolean loading = mData.isLoading();
        if (loading != mLoading) {
            mLoading = loading;
            notifyLoadingChanged();
        }
    }

    private void updateAvailable() {
        int available = mData.available();
        if (available != mAvailable) {
            mAvailable = available;
            notifyAvailableChanged();
        }
    }

    private void updateDataObserver() {
        if (mObservingData && getDataObserverCount() <= 0) {
            mData.unregisterDataObserver(mDataObserver);
            mObservingData = false;
        } else if (!mObservingData && getDataObserverCount() > 0) {
            mData.registerDataObserver(mDataObserver);
            mObservingData = true;
            invalidateEntireIndex();
        }
    }

    private void updateLoadingObserver() {
        if (mObservingLoading && getLoadingObserverCount() <= 0) {
            mData.unregisterLoadingObserver(mLoadingObserver);
            mObservingLoading = false;
        } else if (!mObservingLoading && getLoadingObserverCount() > 0) {
            mData.registerLoadingObserver(mLoadingObserver);
            mObservingLoading = true;
            updateLoading();
        }
    }

    private void updateAvailableObserver() {
        if (mObservingAvailable && getAvailableObserverCount() <= 0) {
            mData.unregisterAvailableObserver(mAvailableObserver);
            mObservingAvailable = false;
        } else if (!mObservingAvailable && getAvailableObserverCount() > 0) {
            mData.registerAvailableObserver(mAvailableObserver);
            mObservingAvailable = true;
            updateAvailable();
        }
    }

    private void updateErrorObserver() {
        if (mObservingError && getErrorObserverCount() <= 0) {
            mData.unregisterErrorObserver(mErrorObserver);
            mObservingError = false;
        } else if (!mObservingError && getErrorObserverCount() > 0) {
            mData.registerErrorObserver(mErrorObserver);
            mObservingError = true;
        }
    }

    private void unregisterAll() {
        if (mObservingData) {
            mData.unregisterDataObserver(mDataObserver);
            mObservingData = false;
        }
        if (mObservingLoading) {
            mData.unregisterLoadingObserver(mLoadingObserver);
            mObservingLoading = false;
        }
        if (mObservingAvailable) {
            mData.unregisterAvailableObserver(mAvailableObserver);
            mObservingAvailable = false;
        }
        if (mObservingError) {
            mData.unregisterErrorObserver(mErrorObserver);
            mObservingError = false;
        }
    }

    private void buildCompleteIndex() {
        changeIndexRange(0, mData.size(), false);
    }

    private void changeIndexRange(final int innerPositionStart, final int itemCount, boolean notify) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            boolean include = apply(t);
            // Check for existing mapping.
            int outerPosition = mIndex.indexOfKey(innerPosition);
            if (outerPosition >= 0) {
                // Mapping already exists.
                if (include) {
                    // Item should be included. Overwrite mapping and notify of a change.
                    mIndex.put(innerPosition);
                    if (notify) {
                        notifyItemChanged(outerPosition);
                    }
                } else {
                    // Item shouldn't be included. Remove mapping and notify of removal.
                    mIndex.removeAt(outerPosition);
                    if (notify) {
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
                    if (notify) {
                        notifyItemInserted(insertionIndex);
                    }
                }
            }
        }
    }

    private void insertIndexRange(final int innerPositionStart, final int itemCount, boolean notify) {
        // Take advantage of indexOfKey() binary search result value to find out what the mapping should be.
        int idx = mIndex.indexOfKey(innerPositionStart);
        int insertionIndex = idx >= 0 ? idx : -idx - 1;
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            T t = mData.get(innerPosition);
            if (apply(t)) {
                mIndex.put(innerPosition);
                if (notify) {
                    notifyItemInserted(insertionIndex);
                }
                insertionIndex++;
            }
        }
    }

    private void removeIndexRange(final int innerPositionStart, final int itemCount, boolean notify) {
        for (int innerPosition = innerPositionStart; innerPosition < innerPositionStart + itemCount; innerPosition++) {
            int outerPosition = mIndex.indexOfKey(innerPosition);
            if (outerPosition >= 0) {
                mIndex.delete(innerPosition);
                if (notify) {
                    notifyItemRemoved(outerPosition);
                }
            }
        }
    }

    private void moveIndexRange(final int innerFromPosition, final int innerToPosition, final int itemCount, boolean notify) {
        // Calculate outer "from" position by skipping past excluded items.
        int outerFromPosition = -1;
        for (int i = 0; i < itemCount; i++) {
            int outerPosition = mIndex.indexOfKey(innerFromPosition + i);
            if (outerFromPosition == -1) {
                outerFromPosition = outerPosition;
            }
        }

        // Remove indexes from "from" range.
        for (int i = 0; i < itemCount; i++) {
            mIndex.delete(innerFromPosition + i);
        }

        // Offset the items between "from" and "to" ranges.
        if (innerToPosition > innerFromPosition) {
            int start = innerFromPosition + itemCount;
            int count = abs(innerToPosition - innerFromPosition);
            mIndex.offset(start, count, sign(innerFromPosition - innerToPosition) * itemCount);
        } else {
            int start = innerToPosition;
            int count = abs(innerToPosition - innerFromPosition);
            mIndex.offsetReverse(start, count, sign(innerFromPosition - innerToPosition) * itemCount);
        }

        // Add indexes to "to" range.
        // Also count the number of included items that were moved.
        int outerItemCount = 0;
        for (int i = 0; i < itemCount; i++) {
            T t = mData.get(innerToPosition + i);
            if (apply(t)) {
                mIndex.put(innerToPosition + i);
                outerItemCount++;
            }
        }

        // Calculate the outer "to" position by skipping past excluded items.
        int outerToPosition = -1;
        for (int innerPosition = innerToPosition; innerPosition < innerToPosition + itemCount; innerPosition++) {
            int outerPosition = mIndex.indexOfKey(innerPosition);
            if (outerPosition != -1) {
                outerToPosition = outerPosition;
                break;
            }
        }

        if (notify && outerItemCount > 0) {
            notifyItemRangeMoved(outerFromPosition, outerToPosition, outerItemCount);
        }
    }

    private boolean apply(@NonNull T t) {
        return mPredicate.apply(t);
    }

    private static int sign(int v) {
        if (v > 0) {
            return +1;
        } else if (v < 0) {
            return -1;
        }
        return 0;
    }

    private static final class Index {

        // TODO: Replace with TreeSet<Integer>, or an ArrayList<Integer>, where we do our own binary searches.
        @NonNull
        private final SparseIntArray mArray = new SparseIntArray();

        int size() {
            return mArray.size();
        }

        void put(int key) {
            mArray.put(key, 0);
        }

        void delete(int key) {
            mArray.delete(key);
        }

        int indexOfKey(int key) {
            return mArray.indexOfKey(key);
        }

        int keyAt(int index) {
            return mArray.keyAt(index);
        }

        void removeAt(int index) {
            mArray.removeAt(index);
        }

        void offset(int start, int count, int offset) {
            for (int i = start; i < start + count; i++) {
                int index = mArray.indexOfKey(i);
                if (index >= 0) {
                    mArray.removeAt(index);
                    mArray.put(i + offset, 0);
                }
            }
        }

        void offsetReverse(int start, int count, int offset) {
            for (int i = start + count - 1; i >= start; i--) {
                int index = mArray.indexOfKey(i);
                if (index >= 0) {
                    mArray.removeAt(index);
                    mArray.put(i + offset, 0);
                }
            }
        }
    }
}
