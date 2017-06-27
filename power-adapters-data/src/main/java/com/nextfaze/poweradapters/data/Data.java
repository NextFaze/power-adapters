package com.nextfaze.poweradapters.data;

import android.database.Cursor;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.UiThread;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Predicate;
import com.nextfaze.poweradapters.RowMapper;
import com.nextfaze.poweradapters.internal.DataObservable;

import java.util.AbstractList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.nextfaze.poweradapters.data.ImmutableData.emptyImmutableData;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/**
 * Provides access to a (possibly asynchronously loaded) list of elements.
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
 * </p>
 */
public abstract class Data<T> implements Iterable<T> {

    /** Flag indicating the intent to present information in a user interface. */
    public static final int FLAG_PRESENTATION = 1;

    public static final int UNKNOWN = -1;

    @NonNull
    final DataObservable mDataObservable = new DataObservable();

    @NonNull
    final AvailableObservable mAvailableObservable = new AvailableObservable();

    @NonNull
    final LoadingObservable mLoadingObservable = new LoadingObservable();

    @NonNull
    final ErrorObservable mErrorObservable = new ErrorObservable();

    @NonNull
    private final List<T> mList = new AbstractList<T>() {
        @Override
        public T get(int location) {
            return Data.this.get(location);
        }

        @Override
        public int size() {
            return Data.this.size();
        }
    };

    @NonNull
    private final CoalescingPoster mPoster = new CoalescingPoster();

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
        mDataObservable.registerObserver(dataObserver);
        if (mDataObservable.getObserverCount() == 1) {
            onFirstDataObserverRegistered();
        }
    }

    @UiThread
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        mDataObservable.unregisterObserver(dataObserver);
        if (mDataObservable.getObserverCount() == 0) {
            onLastDataObserverUnregistered();
        }
    }

    @UiThread
    public void registerAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservable.registerObserver(availableObserver);
    }

    @UiThread
    public void unregisterAvailableObserver(@NonNull AvailableObserver availableObserver) {
        mAvailableObservable.unregisterObserver(availableObserver);
    }

    @UiThread
    public void registerLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservable.registerObserver(loadingObserver);
    }

    @UiThread
    public void unregisterLoadingObserver(@NonNull LoadingObserver loadingObserver) {
        mLoadingObservable.unregisterObserver(loadingObserver);
    }

    @UiThread
    public void registerErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservable.registerObserver(errorObserver);
    }

    @UiThread
    public void unregisterErrorObserver(@NonNull ErrorObserver errorObserver) {
        mErrorObservable.unregisterObserver(errorObserver);
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
        return mDataObservable.getObserverCount();
    }

    /** Returns the number of registered loading observers. */
    protected final int getLoadingObserverCount() {
        return mLoadingObservable.getObserverCount();
    }

    /** Returns the number of registered available observers. */
    protected final int getAvailableObserverCount() {
        return mAvailableObservable.getObserverCount();
    }

    /** Returns the number of registered error observers. */
    protected final int getErrorObserverCount() {
        return mErrorObservable.getObserverCount();
    }

    /** For internal use only. */
    @RestrictTo(LIBRARY_GROUP)
    @NonNull
    protected final DataObservable getDataObservable() {
        return mDataObservable;
    }

    /**
     * Notify any registered observers that the data set has changed.
     * <p>
     * <p>There are two different classes of data change events, item changes and structural
     * changes. Item changes are when a single item has its data updated but no positional
     * changes have occurred. Structural changes are when items are inserted, removed or moved
     * within the data set.</p>
     * <p>
     * <p>This event does not specify what about the data set has changed, forcing
     * any observers to assume that all existing items and structure may no longer be valid.
     * @see #notifyItemChanged(int)
     * @see #notifyItemInserted(int)
     * @see #notifyItemRemoved(int)
     * @see #notifyItemRangeChanged(int, int)
     * @see #notifyItemRangeInserted(int, int)
     * @see #notifyItemRangeRemoved(int, int)
     */
    protected final void notifyDataSetChanged() {
        mDataObservable.notifyDataSetChanged();
    }

    /**
     * Notify any registered observers that the item at <code>position</code> has changed.
     * <p>
     * <p>This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at <code>position</code> is out of date and should be updated.
     * The item at <code>position</code> retains the same identity.</p>
     * @param position Position of the item that has changed
     * @see #notifyItemRangeChanged(int, int)
     */
    protected final void notifyItemChanged(int position) {
        mDataObservable.notifyItemChanged(position);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items starting at
     * position <code>positionStart</code> have changed.
     * <p>
     * <p>This is an item change event, not a structural change event. It indicates that
     * any reflection of the data in the given position range is out of date and should
     * be updated. The items in the given range retain the same identity.</p>
     * <p>
     * Does nothing if {@code itemCount} is zero.
     * @param positionStart Position of the first item that has changed
     * @param itemCount Number of items that have changed
     * @see #notifyItemChanged(int)
     */
    protected final void notifyItemRangeChanged(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeChanged(positionStart, itemCount);
    }

    /**
     * Notify any registered observers that the item reflected at <code>position</code>
     * has been newly inserted. The item previously at <code>position</code> is now at
     * position <code>position + 1</code>.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     * @param position Position of the newly inserted item in the data set
     * @see #notifyItemRangeInserted(int, int)
     */
    protected final void notifyItemInserted(int position) {
        mDataObservable.notifyItemInserted(position);
    }

    /**
     * Notify any registered observers that the currently reflected <code>itemCount</code>
     * items starting at <code>positionStart</code> have been newly inserted. The items
     * previously located at <code>positionStart</code> and beyond can now be found starting
     * at position <code>positionStart + itemCount</code>.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.</p>
     * <p>
     * Does nothing if {@code itemCount} is zero.
     * @param positionStart Position of the first item that was inserted
     * @param itemCount Number of items inserted
     * @see #notifyItemInserted(int)
     */
    protected final void notifyItemRangeInserted(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeInserted(positionStart, itemCount);
    }

    /**
     * Notify any registered observers that the item reflected at <code>fromPosition</code>
     * has been moved to <code>toPosition</code>.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     * @param fromPosition Previous position of the item.
     * @param toPosition New position of the item.
     */
    protected final void notifyItemMoved(int fromPosition, int toPosition) {
        mDataObservable.notifyItemMoved(fromPosition, toPosition);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items reflected at <code>fromPosition</code>
     * have been moved to <code>toPosition</code>.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     * Does nothing if {@code itemCount} is zero.
     * @param fromPosition Previous position of the items.
     * @param toPosition New position of the items.
     * @param itemCount The number of items moved.
     */
    protected final void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        mDataObservable.notifyItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    /**
     * Notify any registered observers that the item previously located at <code>position</code>
     * has been removed from the data set. The items previously located at and after
     * <code>position</code> may now be found at <code>oldPosition - 1</code>.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.</p>
     * @param position Position of the item that has now been removed
     * @see #notifyItemRangeRemoved(int, int)
     */
    protected final void notifyItemRemoved(int position) {
        mDataObservable.notifyItemRemoved(position);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items previously
     * located at <code>positionStart</code> have been removed from the data set. The items
     * previously located at and after <code>positionStart + itemCount</code> may now be found
     * at <code>oldPosition - itemCount</code>.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the data
     * set are still considered up to date and will not be rebound, though their positions
     * may be altered.</p>
     * <p>
     * Does nothing if {@code itemCount} is zero.
     * @param positionStart Previous position of the first item that was removed
     * @param itemCount Number of items removed from the data set
     */
    protected final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeRemoved(positionStart, itemCount);
    }

    /** Dispatch a available change notification on the UI thread. */
    protected final void notifyAvailableChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAvailableObservable.notifyAvailableChanged();
            }
        });
    }

    /** Dispatch a loading change notification on the UI thread. */
    protected final void notifyLoadingChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoadingObservable.notifyLoadingChanged();
            }
        });
    }

    /** Dispatch an error notification on the UI thread. */
    protected final void notifyError(@NonNull final Throwable e) {
        //noinspection ThrowableResultOfMethodCallIgnored
        checkNotNull(e, "e");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mErrorObservable.notifyError(e);
            }
        });
    }

    /** Runs a task on the UI thread. If caller thread is the UI thread, the task is executed immediately. */
    protected final void runOnUiThread(@NonNull Runnable runnable) {
        checkNotNull(runnable, "runnable");
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mPoster.post(runnable);
        }
    }

    @SuppressWarnings("unchecked")
    @CheckResult
    @NonNull
    public final <O> Data<O> compose(@NonNull Transformer<? super T, ? extends O> transformer) {
        checkNotNull(transformer, "transformer");
        return ((Transformer<T, O>) transformer).transform(this);
    }

    /** Filters this data based on a predicate. */
    @CheckResult
    @NonNull
    public final Data<T> filter(@NonNull Predicate<? super T> predicate) {
        return new FilterData<>(this, predicate);
    }

    /** Filter this data by class. The resulting elements are guaranteed to be of the given type. */
    @CheckResult
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

    /** Sorts this data using the specified {@link Comparator}. */
    @CheckResult
    @NonNull
    public final Data<T> sort(@NonNull Comparator<? super T> comparator) {
        return new SortData<>(this, comparator);
    }

    /** Transforms this data by applying {@code function} to each element. */
    @CheckResult
    @NonNull
    public final <O> Data<O> transform(@NonNull Function<? super T, ? extends O> function) {
        return new TransformData<>(this, function);
    }

    @CheckResult
    @NonNull
    public final Data<T> offset(int offset) {
        if (offset <= 0) {
            return this;
        }
        return new OffsetData<>(this, offset);
    }

    @CheckResult
    @NonNull
    public final Data<T> limit(int limit) {
        if (limit == Integer.MAX_VALUE) {
            return this;
        }
        return new LimitData<>(this, limit);
    }

    // TODO: Document asList() as permitting mutability in subclasses.

    /** Returns this {@link Data} as an immutable list. */
    @CheckResult
    @NonNull
    public List<T> asList() {
        return mList;
    }

    /**
     * Creates a {@link Data} whose elements will be populated by invoking the specified loader function in a
     * worker thread.
     * @param loader The function to be invoked to load the list of elements.
     * @param <T> The type of element presented by the returned data.
     * @return A {@linkplain Data} instance that will present the elements retrieved via the loader function.
     */
    @NonNull
    public static <T> Data<T> fromList(@NonNull Callable<List<? extends T>> loader) {
        return fromList(loader, DataExecutors.defaultExecutor());
    }

    /**
     * Creates a {@link Data} whose elements will be populated by invoking the specified loader function in a
     * worker thread with the specified {@link ExecutorService}.
     * @param loader The function to be invoked to load the list of elements.
     * @param executor The {@linkplain ExecutorService} used to invoke the loader function.
     * @param <T> The type of element presented by the returned data.
     * @return A {@linkplain Data} instance that will present the elements retrieved via the loader function.
     */
    @NonNull
    public static <T> Data<T> fromList(@NonNull final Callable<List<? extends T>> loader,
                                       @NonNull ExecutorService executor) {
        return new ArrayData<T>(executor) {
            @NonNull
            @Override
            protected List<? extends T> load() throws Throwable {
                return loader.call();
            }
        };
    }

    /**
     * Creates a {@linkplain Data} that presents elements of a {@link Cursor} retrieved by invoking the specified loader
     * function in a worker thread. {@link T} instances are mapped using the specified row mapper function.
     * The cursor's position will be pre-configured - callers don't need to set it.
     * <p>
     * Note that the returned {@linkplain Data} manages the {@linkplain Cursor} instances itself, and callers should
     * never close cursors returned by the loader function.
     * @param loader The function to be invoked to load the cursor.
     * @param rowMapper The function to be invoked to map rows from the {@linkplain Cursor} to instances of {@link T}.
     * @param <T> The type of element presented by the returned data.
     * @return A {@linkplain Data} instance that will present the cursor elements.
     */
    @NonNull
    public static <T> Data<T> fromCursor(@NonNull Callable<? extends Cursor> loader,
                                         @NonNull RowMapper<T> rowMapper) {
        return fromCursor(loader, rowMapper, DataExecutors.defaultExecutor());
    }

    /**
     * Creates a {@linkplain Data} that presents elements of a {@link Cursor} retrieved by invoking the specified loader
     * function in a worker thread with the specified {@link ExecutorService}. {@link T} instances are mapped using the
     * specified row mapper function.
     * The cursor's position will be pre-configured - callers don't need to set it.
     * <p>
     * Note that the returned {@linkplain Data} manages the {@linkplain Cursor} instances itself, and callers should
     * never close cursors returned by the loader function.
     * @param loader The function to be invoked to load the cursor.
     * @param rowMapper The function to be invoked to map rows from the {@linkplain Cursor} to instances of {@link T}.
     * @param executor The {@linkplain ExecutorService} used to invoke the cursor loader function.
     * @param <T> The type of element presented by the returned data.
     * @return A {@linkplain Data} instance that will present the cursor elements.
     */
    @NonNull
    public static <T> Data<T> fromCursor(@NonNull final Callable<? extends Cursor> loader,
                                         @NonNull final RowMapper<T> rowMapper,
                                         @NonNull ExecutorService executor) {
        return new CursorData<T>(executor) {
            @NonNull
            @Override
            protected T map(@NonNull Cursor cursor) {
                return rowMapper.map(cursor);
            }

            @NonNull
            @Override
            protected Cursor load() throws Throwable {
                return loader.call();
            }
        };
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
