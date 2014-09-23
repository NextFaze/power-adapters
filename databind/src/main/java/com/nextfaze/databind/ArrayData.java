package com.nextfaze.databind;

import com.nextfaze.concurrent.Task;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

// TODO: Make thread-safe.
@Accessors(prefix = "m")
public abstract class ArrayData<T> extends AbstractData<T> {

    @Nullable
    private ArrayList<T> mData;

    @Nullable
    private Task<?> mTask;

    private boolean mDirty = true;

    protected ArrayData() {
    }

    @Override
    public final int size() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public final boolean isEmpty() {
        return size() == 0;
    }

    @NonNull
    @Override
    public final T get(int position) {
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
            notifyChanged();
            return true;
        }
        return false;
    }

    public final boolean remove(@NonNull T t) {
        if (mData != null && mData.remove(t)) {
            notifyChanged();
            return true;
        }
        return false;
    }

    @Override
    public final boolean isLoading() {
        return mTask != null;
    }

    @NonNull
    protected abstract List<? extends T> loadData() throws Exception;

    @Override
    protected final void onShown() {
        loadDataIfAppropriate();
    }

    @Override
    protected final void onHidden() {
        // Cancel any existing data loads, since we no longer care now we're hidden.
        if (mTask != null) {
            mTask.cancel();
            mTask = null;
        }
    }

    @Override
    protected final void onHideTimeout() {
        mData = null;
    }

    private void loadDataIfAppropriate() {
        // We only start loading the data if it's not already loading, and we're shown.
        // If we're not shown we don't care about the data.
        // Only load if data is marked as dirty.
        if (mDirty && mTask == null && isShown()) {
            mTask = new Task<List<? extends T>>() {
                @Override
                protected List<? extends T> call() throws Throwable {
                    return loadData();
                }

                @Override
                protected void onSuccess(@NonNull List<? extends T> data) throws Throwable {
                    mData = new ArrayList<T>(data);
                    mDirty = false;
                    mTask = null;
                    notifyLoadingChanged();
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
}
