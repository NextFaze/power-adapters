package com.nextfaze.databind;

import android.util.SparseArray;
import com.nextfaze.concurrent.Task;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Math.max;
import static java.lang.Math.min;

@Slf4j
@Accessors(prefix = "m")
public abstract class PagedArrayData<T> extends AbstractData<T> {

    public static final int ROWS_PER_REQUEST_DEFAULT = 20;

    private static final int UNKNOWN = -1;
    private static final int ROWS_PER_REQUEST_MIN = 1;
    private static final int ROWS_PER_REQUEST_MAX = MAX_VALUE;

    /** The backing storage. May contain safely contains holes. */
    @NonNull
    private final SparseArray<T> mData = new SparseArray<>();

    /** References the current async load task. */
    @Nullable
    private Task<?> mTask;

    /** The number of rows to be requested. */
    private final int mRowsPerRequest;

    @Getter
    private int mTotal = UNKNOWN;

    private int mLastLoadedIndex;

    protected PagedArrayData() {
        this(ROWS_PER_REQUEST_DEFAULT);
    }

    protected PagedArrayData(int rowsPerRequest) {
        mRowsPerRequest = clamp(rowsPerRequest, ROWS_PER_REQUEST_MIN, ROWS_PER_REQUEST_MAX);
    }

    @NonNull
    @Override
    public final T get(int position) {
        // Requested end of data? Time to load more.
        if (position >= mData.size() - 1) {
            loadAsNeeded();
        }
        return mData.get(position);
    }

    @Override
    public final int size() {
        return mData.size();
    }

    @Override
    public final boolean isLoading() {
        return mTask != null;
    }

    public final void clear() {
        mTotal = UNKNOWN;
        mLastLoadedIndex = 0;
        mData.clear();
        notifyChanged();
    }

    // TODO: "Load next" method, to hook up to buttons.

    @NonNull
    protected abstract Page<T> load(int offset, int count) throws Exception;

    @Override
    protected void onShown(long millisHidden) {
        // TODO: If hidden for too long, invalidate entire range.
        loadAsNeeded();
    }

    @Override
    protected void onHidden(long millisShown) {
        // TODO: Cancel after a delay.
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
    }

    private void loadAsNeeded() {
        int remaining;
        if (mTotal != UNKNOWN) {
            remaining = mTotal - mLastLoadedIndex;
        } else {
            remaining = MAX_VALUE;
        }

        final int offset = mLastLoadedIndex;
        final int count = min(mRowsPerRequest, remaining);

        if (mTask == null && isShown() && count > 0) {
            mTask = new Task<Page<T>>() {
                @Override
                @NonNull
                public Page<T> call() throws Exception {
                    return load(offset, count);
                }

                @Override
                protected void onSuccess(@NonNull Page<T> page) throws Exception {
                    mTotal = page.getTotal();
                    mLastLoadedIndex = page.getOffset() + page.getCount();
                    store(page);
                    notifyChanged();
                }

                @Override
                protected void onCanceled() throws Throwable {
                    mTask = null;
                    notifyLoadingChanged();
                }

                @Override
                protected void onFailure(@NonNull Throwable e) throws Throwable {
                    mTask = null;
                    notifyLoadingChanged();
                    notifyError(e);
                }
            };
            notifyLoadingChanged();
            mTask.execute();
        }
    }

    private boolean store(@NonNull Page<T> page) {
        boolean changed = false;
        List<T> list = page.getContents();
        int size = list.size();
        for (int i = 0; i < size && i < page.getCount(); ++i) {
            T item = list.get(i);
            int position = page.getOffset() + i;
            changed |= mData.get(position) != null;
            mData.put(position, item);
        }
        return changed;
    }

    private static int clamp(int v, int min, int max) {
        return max(min(max, v), min);
    }

    @Getter
    @Accessors(prefix = "m")
    public static final class Page<T> {

        @NonNull
        private final List<T> mContents;

        private final int mOffset;
        private final int mCount;
        private final int mTotal;

        public Page(@NonNull List<T> contents, int offset, int count, int total) {
            mContents = contents;
            mOffset = offset;
            mCount = count;
            mTotal = total;
        }

        @NonNull
        public static <T> Page<T> emptyResult() {
            return new Page<>(Collections.<T>emptyList(), 0, 0, 0);
        }
    }
}
