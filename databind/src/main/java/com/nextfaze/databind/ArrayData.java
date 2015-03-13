package com.nextfaze.databind;

import com.nextfaze.concurrent.Task;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// TODO: Make thread-safe.
@Accessors(prefix = "m")
public abstract class ArrayData<T> extends AbstractData<T> {

    private static final Logger log = LoggerFactory.getLogger(ArrayData.class);

    @Nullable
    private ArrayList<T> mData;

    @Nullable
    private Task<?> mTask;

    /** Automatically invalidate contents if data is hidden for the specified duration. */
    @Getter
    @Setter
    private long mAutoInvalidateDelay = Long.MAX_VALUE;

    private boolean mDirty = true;

    protected ArrayData() {
    }

    @Override
    public final int size() {
        return mData != null ? mData.size() : 0;
    }

    @NonNull
    @Override
    public final T get(int position, int flags) {
        //noinspection ConstantConditions
        return mData.get(position);
    }

    public final void invalidate() {
        mDirty = true;
        loadDataIfAppropriate();
    }

    public final void clear() {
        mData = null;
        invalidate();
    }

    public final boolean add(@NonNull T t) {
        if (mData == null) {
            mData = new ArrayList<T>();
        }
        if (mData.add(t)) {
            notifyDataChanged();
            return true;
        }
        return false;
    }

    public final boolean remove(@NonNull T t) {
        if (mData != null && mData.remove(t)) {
            notifyDataChanged();
            return true;
        }
        return false;
    }

    @Override
    public final boolean isLoading() {
        return mTask != null;
    }

    @NonNull
    protected abstract List<? extends T> loadData() throws Throwable;

    @Override
    protected final void onShown(long millisHidden) {
        // TODO: If an error occurred, we always want to invalidate the data?
        if (millisHidden >= mAutoInvalidateDelay) {
            log.trace("Automatically invalidating due to auto-invalidate delay being reached or exceeded");
            mDirty = true;
        }
        loadDataIfAppropriate();
    }

    @Override
    protected final void onHidden(long millisShown) {
    }

    @Override
    protected void onHideTimeout() {
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    private void loadDataIfAppropriate() {
        // We only start loading the data if it's not already loading, and we're shown.
        // If we're not shown we don't care about the data.
        // Only load if data is marked as dirty.
        if (mDirty && mTask == null && isShown()) {
            mDirty = false;
            // TODO: Replace use of Task, so we stop depending on NextFaze Concurrent library.
            mTask = new Task<List<? extends T>>() {
                @Override
                protected List<? extends T> call() throws Throwable {
                    return loadData();
                }

                @Override
                protected void onSuccess(@NonNull List<? extends T> data) throws Throwable {
                    mData = new ArrayList<T>(data);
                    mTask = null;
                    notifyLoadingChanged();
                    notifyDataChanged();
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

                @Override
                protected void onFinally() throws Throwable {
                    loadDataIfAppropriate();
                }
            };
            notifyLoadingChanged();
            mTask.execute();
        }
    }
}
