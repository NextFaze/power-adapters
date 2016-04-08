package com.nextfaze.powerdata;

import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.Iterator;

/**
 * Skeleton {@link Data} implementation that provides observer management and sensible default method implementations.
 */
@Accessors(prefix = "m")
public abstract class AbstractData<T> implements Data<T> {

    @NonNull
    private final DataObservers mDataObservers = new DataObservers();

    @NonNull
    private final AvailableObservers mAvailableObservers = new AvailableObservers();

    @NonNull
    private final LoadingObservers mLoadingObservers = new LoadingObservers();

    @NonNull
    private final ErrorObservers mErrorObservers = new ErrorObservers();

    @NonNull
    private final CoalescingPoster mPoster = new CoalescingPoster();

    //region Observer Registration
    @Override
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.register(dataObserver);
        if (mDataObservers.size() == 1) {
            onFirstDataObserverRegistered();
        }
    }

    @Override
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.unregister(dataObserver);
        if (mDataObservers.size() == 0) {
            onLastDataObserverUnregistered();
        }
    }

    @Override
    public void registerAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservers.register(availableObserver);
    }

    @Override
    public void unregisterAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservers.unregister(availableObserver);
    }

    @Override
    public void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.register(loadingObserver);
    }

    @Override
    public void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.unregister(loadingObserver);
    }

    @Override
    public void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.register(errorObserver);
    }

    @Override
    public void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.unregister(errorObserver);
    }
    //endregion

    @NonNull
    @Override
    public final T get(int position) {
        return get(position, 0);
    }

    /**
     * Returns {@link #UNKNOWN} by default.
     * @see Data#available()
     */
    @Override
    public int available() {
        return UNKNOWN;
    }

    @Override
    public boolean isEmpty() {
        return size() <= 0;
    }

    @Override
    public Iterator<T> iterator() {
        return new DataIterator<>(this);
    }

    /** Called when the first {@link DataObserver} is registered with this instance. */
    @UiThread
    @CallSuper
    protected void onFirstDataObserverRegistered() {
    }

    /** Called when the last {@link DataObserver} is unregistered from this instance. */
    @UiThread
    @CallSuper
    protected void onLastDataObserverUnregistered() {
    }

    /** Returns the number of registered data observers. */
    protected final int getDataObserverCount() {
        return mDataObservers.size();
    }

    /** Returns the number of registered loading observers. */
    protected final int getLoadingObserverCount() {
        return mLoadingObservers.size();
    }

    /** Returns the number of registered available observers. */
    protected final int getAvailableObserverCount() {
        return mAvailableObservers.size();
    }

    /** Returns the number of registered error observers. */
    protected final int getErrorObserverCount() {
        return mErrorObservers.size();
    }

    /** Dispatch a data change notification on the UI thread. */
    protected void notifyDataChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyDataChanged();
            }
        });
    }

    protected void notifyItemChanged(final int position) {
        notifyItemRangeChanged(position, 1);
    }

    protected void notifyItemRangeChanged(final int positionStart, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeChanged(positionStart, itemCount);
            }
        });
    }

    protected void notifyItemInserted(int position) {
        notifyItemRangeInserted(position, 1);
    }

    protected void notifyItemRangeInserted(final int positionStart, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeInserted(positionStart, itemCount);
            }
        });
    }

    protected void notifyItemMoved(int fromPosition, int toPosition) {
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    protected void notifyItemRangeMoved(final int fromPosition, final int toPosition, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        });
    }

    protected void notifyItemRemoved(int position) {
        notifyItemRangeRemoved(position, 1);
    }

    protected void notifyItemRangeRemoved(final int positionStart, final int itemCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyItemRangeRemoved(positionStart, itemCount);
            }
        });
    }

    /** Dispatch a available change notification on the UI thread. */
    protected void notifyAvailableChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAvailableObservers.notifyAvailableChanged();
            }
        });
    }

    /** Dispatch a loading change notification on the UI thread. */
    protected void notifyLoadingChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingObservers.notifyLoadingChanged();
            }
        });
    }

    /** Dispatch an error notification on the UI thread. */
    protected void notifyError(@NonNull final Throwable e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorObservers.notifyError(e);
            }
        });
    }

    /** Runs a task on the UI thread. If caller thread is the UI thread, the task is executed immediately. */
    protected void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mPoster.post(runnable);
        }
    }
}
