package com.nextfaze.poweradapters.data;

import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Predicate;
import lombok.NonNull;

import java.util.Iterator;

import static com.nextfaze.poweradapters.data.ImmutableData.emptyImmutableData;

/**
 * Provides access to an asynchronously loaded list of elements.
 * <h3>Loading</h3>
 * <p>
 * A {@code Data} instance may be in a loading state, which it broadcasts to interested observers so they can present
 * loading indicators.
 * </p>
 * <h3>Errors</h3>
 * <p>
 * It also broadcasts errors it encounters while loading. The occurrence of an error doesn't imply any change in state.
 * </p>
 * <h3>Threading</h3>
 * <p>
 * In general this class is not thread-safe. It's intended to be accessed from the UI thread only.
 * <h3>Notifications</h3>
 * Change notifications must be dispatched BEFORE the other notifications.
 * </p>
 */
public abstract class Data<T> implements Iterable<T> {

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

    /** Flag indicating the intent to present information in a user interface. */
    public static final int FLAG_PRESENTATION = 1;

    public static final int UNKNOWN = -1;

    /**
     * Retrieve the element at the specified position. Equivalent to calling {@link #get(int, int)} without any flags.
     * @param position The position at which to retrieve the value.
     * @return The value at the specified position, never {@code null}.
     * @throws RuntimeException If the element is out of bounds or can't be retrieved.
     */
    @UiThread
    @NonNull
    public final T get(int position) {
        return get(position, 0);
    }

    /**
     * Retrieve the element at the specified position.
     * The return value for a given {@code position} must never change:
     * <ul>
     * <li>Outside the UI thread</li>
     * <li>Without a corresponding change notification</li>
     * </ul>
     * @param position The position at which to retrieve the value.
     * @param flags Bit field containing flags with extra information about this request for an element.
     * @return The value at the specified position, never {@code null}.
     * @throws RuntimeException If the element is out of bounds or can't be retrieved.
     */
    @UiThread
    @NonNull
    public abstract T get(int position, int flags);

    /**
     * The number of elements in this data instance.
     * The return value must never change:
     * <ul>
     * <li>Outside the UI thread</li>
     * <li>Without a corresponding change notification</li>
     * </ul>
     * @return The number of elements, always {@code >= 0}.
     */
    @UiThread
    public abstract int size();

    /**
     * Indicates how many more elements are available to be loaded relative to the current data set. Implementations
     * can override this if they wish to convey an incomplete data set to observers.
     * The return value must never change:
     * <ul>
     * <li>Outside the UI thread</li>
     * <li>Without a corresponding available notification</li>
     * </ul>
     * <p>
     * By default, returns {@link #UNKNOWN}.
     * @return {@link #UNKNOWN} if the value is unknown, {@link Integer#MAX_VALUE} if it's known there are more elements
     * available, but not how many. {@code 0} indicates no more elements are available to be loaded.
     */
    @UiThread
    public int available() {
        return UNKNOWN;
    }

    /** Simply returns if {@link #size()} {@code == 0}. The same threading constraints apply. */
    @UiThread
    public final boolean isEmpty() {
        return size() <= 0;
    }

    /** If {@code true}, indicates the data is currently loading more elements, or updating existing ones. */
    @UiThread
    public abstract boolean isLoading();

    /**
     * Marks existing elements as invalid, such that they are not reloaded immediately, but at the next suitable time,
     * such as when clients resume observation.
     */
    @UiThread
    public abstract void invalidate();

    /** Reloads the elements without clearing them first. */
    @UiThread
    public abstract void refresh();

    /** Clears then refreshes the elements. */
    @UiThread
    public abstract void reload();

    @UiThread
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.register(dataObserver);
        if (mDataObservers.size() == 1) {
            onFirstDataObserverRegistered();
        }
    }

    @UiThread
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservers.unregister(dataObserver);
        if (mDataObservers.size() == 0) {
            onLastDataObserverUnregistered();
        }
    }

    @UiThread
    public void registerAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservers.register(availableObserver);
    }

    @UiThread
    public void unregisterAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservers.unregister(availableObserver);
    }

    @UiThread
    public void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.register(loadingObserver);
    }

    @UiThread
    public void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservers.unregister(loadingObserver);
    }

    @UiThread
    public void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.register(errorObserver);
    }

    @UiThread
    public void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservers.unregister(errorObserver);
    }

    @UiThread
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
    protected void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDataObservers.notifyDataSetChanged();
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

    @SuppressWarnings("unchecked")
    @NonNull
    public final <O> Data<O> compose(@NonNull Transformer<? super T, ? extends O> transformer) {
        return ((Transformer<T, O>) transformer).transform(this);
    }

    /** Filters the specified data based on a predicate. */
    @NonNull
    public final Data<T> filter(@NonNull Predicate<? super T> predicate) {
        return new FilterData<>(this, predicate);
    }

    /** Filter the specified data by class. The resulting elements are guaranteed to be of the given type. */
    @NonNull
    public final <O> Data<O> filter(@NonNull final Class<O> type) {
        //noinspection unchecked
        return (Data<O>) new FilterData<>(this, new Predicate<Object>() {
            @Override
            public boolean apply(Object o) {
                return type.isInstance(o);
            }
        });
    }

    /** Transforms the specified data by applying {@code function} to each element. */
    @NonNull
    public final <O> Data<O> transform(@NonNull Function<? super T, ? extends O> function) {
        return new TransformData<>(this, function);
    }

    @NonNull
    public final Data<T> offset(int offset) {
        if (offset <= 0) {
            return this;
        }
        return new OffsetData<>(this, offset);
    }

    @NonNull
    public final Data<T> limit(int limit) {
        if (limit == Integer.MAX_VALUE) {
            return this;
        }
        return new LimitData<>(this, limit);
    }

    @NonNull
    public static <T> Data<T> emptyData() {
        return emptyImmutableData();
    }

    public interface Transformer<T, R> {
        @NonNull
        Data<R> transform(@NonNull Data<T> data);
    }
}
