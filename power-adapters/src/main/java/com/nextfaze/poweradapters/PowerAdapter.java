package com.nextfaze.poweradapters;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.internal.DataObservable;

import java.util.Collection;
import java.util.List;

import androidx.annotation.CallSuper;
import androidx.annotation.CheckResult;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.nextfaze.poweradapters.ItemAdapter.toItems;
import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

/**
 * Base class for a composable, view-agnostic adapter.
 * <p>
 * A {@link PowerAdapter} is only required to publish items when at least one observer is registered. Therefore
 * clients should always register an observer before accessing the contents of the adapter.
 */
@SuppressWarnings("WeakerAccess")
public abstract class PowerAdapter {

    /** An adapter with no elements, which never changes. */
    public static final PowerAdapter EMPTY = new PowerAdapter() {
        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public long getItemId(int position) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public Object getItemViewType(int position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEnabled(int position) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bindView(
                @NonNull Container container,
                @NonNull View view,
                @NonNull Holder holder,
                @NonNull List<Object> payloads
        ) {
            throw new UnsupportedOperationException();
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void registerDataObserver(@NonNull DataObserver dataObserver) {
            // No-op; this adapter does not emit any changes.
        }

        @SuppressLint("MissingSuperCall")
        @Override
        public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
            // No-op; this adapter does not emit any changes.
        }
    };

    /** Used to indicate an item has no ID. */
    public static final int NO_ID = -1;

    @NonNull
    final DataObservable mDataObservable = new DataObservable();

    /**
     * Returns the total number of items in the data set hold by the adapter.
     * @return The total number of items in this adapter.
     */
    public abstract int getItemCount();

    /**
     * Returns true if this adapter publishes a unique {@code long} value that can
     * act as a key for the item at a given position in the data set. If that item is relocated
     * in the data set, the ID returned for that item should be the same.
     * @return true if this adapter's items have stable IDs
     */
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Return the stable ID for the item at {@code position}. If {@link #hasStableIds()}
     * would return false this method should return {@link #NO_ID}.
     * @param position Adapter position to query
     * @return the stable ID of the item at position
     */
    public long getItemId(int position) {
        return NO_ID;
    }

    /**
     * Get the type of view for item at {@code position}, for the purpose of view recycling. By default, returns
     * {@code this}, which implies this adapter supports a single view type.
     * @param position The position of the item within the adapter's data set whose view type we
     * want.
     * @return An object that is unique for this type of view for the lifetime of this adapter.
     */
    @NonNull
    public Object getItemViewType(int position) {
        return this;
    }

    /**
     * Returns true if the item at the specified position is not a separator.
     * (A separator is a non-selectable, non-clickable item). By default, returns true.
     * @param position Index of the item
     * @return True if the item is not a separator
     */
    public boolean isEnabled(int position) {
        return true;
    }

    /**
     * Create a new {@link View} of the specified view type, destined for the specified parent {@link ViewGroup}.
     * @param parent The view group the constructed view will be added to.
     * @param viewType The view type object.
     * @return A new view that may be recycled for items of the same view type.
     */
    @NonNull
    public abstract View newView(@NonNull ViewGroup parent, @NonNull Object viewType);

    /**
     * Binds the data associated with {@link Holder#getPosition()} to the specified view.
     * Use {@link Holder#getPosition()} to access the position in the data set.
     * @param container The {@link Container} that owns the view to be bound.
     * @param view The view to bind.
     * @param holder The holder object representing this binding to the view.
     * @param payloads A list of merged payload objects. Can be empty if a full update is required.
     * @see Holder#getPosition()
     * @see Container
     */
    public abstract void bindView(
            @NonNull Container container,
            @NonNull View view,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    );

    /**
     * Registers an observer with this adapter, to be notified of data set changes.
     * <p>
     * A {@link PowerAdapter} is only required to publish items once at least one observer is registered. Therefore
     * clients should always register an observer before accessing the contents of the adapter.
     * <p>
     * Note that it is an error to register the same observer twice, and doing so may result in an
     * {@link IllegalStateException}.
     * @param dataObserver The observer to be registered.
     */
    @CallSuper
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        checkNotNull(dataObserver, "dataObserver");
        mDataObservable.registerObserver(dataObserver);
        if (mDataObservable.getObserverCount() == 1) {
            onFirstObserverRegistered();
        }
    }

    /**
     * De-registers an observer from this adapter.
     * <p>
     * Note that it is an error to unregister the same observer twice, and doing so may result in an
     * {@link IllegalStateException}.
     * @param dataObserver The observer to be unregistered.
     * @see #registerDataObserver(DataObserver)
     */
    @CallSuper
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        checkNotNull(dataObserver, "dataObserver");
        mDataObservable.unregisterObserver(dataObserver);
        if (mDataObservable.getObserverCount() == 0) {
            onLastObserverUnregistered();
        }
    }

    /** Returns the number of registered observers. */
    protected final int getObserverCount() {
        return mDataObservable.getObserverCount();
    }

    /**
     * Called when the first observer has registered with this adapter.
     * @see #registerDataObserver(DataObserver)
     */
    @CallSuper
    protected void onFirstObserverRegistered() {
    }

    /**
     * Called when the last observer has unregistered from this adapter.
     * @see #unregisterDataObserver(DataObserver)
     */
    @CallSuper
    protected void onLastObserverUnregistered() {
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
     * @see #notifyItemChanged(int, Object)
     * @see #notifyItemInserted(int)
     * @see #notifyItemRemoved(int)
     * @see #notifyItemRangeChanged(int, int, Object)
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
     * @param payload Provides optional change payload metadata. Use `null` to identify a "full" update.
     * @see #notifyItemRangeChanged(int, int, Object)
     */
    protected final void notifyItemChanged(int position, @Nullable Object payload) {
        mDataObservable.notifyItemChanged(position, payload);
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
     * @param payload Provides optional change payload metadata. Use `null` to identify a "full" update.
     * @see #notifyItemChanged(int, Object)
     */
    protected final void notifyItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
        mDataObservable.notifyItemRangeChanged(positionStart, itemCount, payload);
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

    /** Applies the specified {@link Transformer} to this adapter, and returns the result. */
    @CheckResult
    @NonNull
    public final PowerAdapter compose(@NonNull Transformer transformer) {
        return checkNotNull(transformer, "transformer").transform(this);
    }

    /**
     * Concatenates the specified adapters with this adapter, and returns the result.
     * @param adapters The adapters to be prepended.
     * @return The resulting composite adapter.
     * @see #concat(PowerAdapter...)
     */
    @CheckResult
    @NonNull
    public final PowerAdapter prepend(@NonNull PowerAdapter... adapters) {
        checkNotNull(adapters, "adapters");
        if (adapters.length == 0) {
            return this;
        }
        return new ConcatAdapterBuilder().addAll(adapters).add(this).build();
    }

    /**
     * Concatenates the specified views with this adapter, and returns the result.
     * @param views The views to be prepended.
     * @return The resulting composite adapter.
     * @see #prepend(PowerAdapter...)
     * @see #concat(PowerAdapter...)
     */
    @CheckResult
    @NonNull
    public final PowerAdapter prepend(@NonNull ViewFactory... views) {
        checkNotNull(views, "views");
        if (views.length == 0) {
            return this;
        }
        return prepend(asAdapter(views));
    }

    /**
     * Concatenates the views represented by the specified layout resources with this adapter, and returns the result.
     * @param layoutResources The layout resources that will be inflated and prepended to this adapter.
     * @return The resulting composite adapter.
     * @see ViewFactories#asViewFactory(int)
     * @see #prepend(PowerAdapter...)
     * @see #concat(PowerAdapter...)
     */
    @CheckResult
    @NonNull
    public final PowerAdapter prepend(@NonNull @LayoutRes int... layoutResources) {
        checkNotNull(layoutResources, "layoutResources");
        if (layoutResources.length == 0) {
            return this;
        }
        return prepend(asAdapter(layoutResources));
    }

    /**
     * Concatenates the specified adapters with this adapter, and returns the result.
     * @param adapters The adapters to be appended.
     * @return The resulting composite adapter.
     * @see #concat(PowerAdapter...)
     */
    @CheckResult
    @NonNull
    public final PowerAdapter append(@NonNull PowerAdapter... adapters) {
        checkNotNull(adapters, "adapters");
        if (adapters.length == 0) {
            return this;
        }
        return new ConcatAdapterBuilder().add(this).addAll(adapters).build();
    }

    /**
     * Concatenates the specified views with this adapter, and returns the result.
     * @param views The views to be appended.
     * @return The resulting composite adapter.
     * @see #prepend(PowerAdapter...)
     * @see #concat(PowerAdapter...)
     */
    @CheckResult
    @NonNull
    public final PowerAdapter append(@NonNull ViewFactory... views) {
        checkNotNull(views, "views");
        if (views.length == 0) {
            return this;
        }
        return append(asAdapter(views));
    }

    /**
     * Concatenates the views represented by the specified layout resources with this adapter, and returns the result.
     * @param layoutResources The layout resources that will be inflated and appended to this adapter.
     * @return The resulting composite adapter.
     * @see ViewFactories#asViewFactory(int)
     * @see #prepend(PowerAdapter...)
     * @see #concat(PowerAdapter...)
     */
    @CheckResult
    @NonNull
    public final PowerAdapter append(@NonNull @LayoutRes int... layoutResources) {
        checkNotNull(layoutResources, "layoutResources");
        if (layoutResources.length == 0) {
            return this;
        }
        return append(asAdapter(layoutResources));
    }

    /**
     * Returns a new adapter that presents all items of this adapter, starting at the specified offset.
     * @param offset The item offset.
     * @return A new adapter.
     */
    @CheckResult
    @NonNull
    public final PowerAdapter offset(int offset) {
        if (offset <= 0) {
            return this;
        }
        if (offset == Integer.MAX_VALUE) {
            return EMPTY;
        }
        return new OffsetAdapter(this, offset);
    }

    /**
     * Returns a new adapter that presents all items of this adapter, up until the specified limit.
     * @param limit The item limit.
     * @return A new adapter.
     */
    @CheckResult
    @NonNull
    public final PowerAdapter limit(int limit) {
        if (limit == Integer.MAX_VALUE) {
            return this;
        }
        if (limit <= 0) {
            return EMPTY;
        }
        return new LimitAdapter(this, limit);
    }

    /**
     * Returns a new adapter that presents the items of this adapter only while the specified condition evaluates to
     * {@code true}.
     * @param condition The condition dictating whether to show the items.
     * @return A new adapter.
     */
    @CheckResult
    @NonNull
    public final PowerAdapter showOnlyWhile(@NonNull Condition condition) {
        checkNotNull(condition, "condition");
        if (condition instanceof ConstantCondition) {
            if (condition.eval()) {
                return this;
            } else {
                return EMPTY;
            }
        }
        return new ConditionalAdapter(this, condition);
    }

    /**
     * Returns a new adapter that wraps each item {@link View} with the specified view. The {@link ViewGroup} must not
     * contain any other child views.
     * @param wrapperView The wrapper {@link ViewGroup} factory.
     * @return A new adapter.
     */
    @CheckResult
    @NonNull
    public PowerAdapter wrapItems(@NonNull ViewFactory wrapperView) {
        return new WrappingAdapter(this, wrapperView);
    }

    /**
     * Returns a new adapter that wraps each item {@link View} with the {@link ViewGroup} inflated by the specified
     * layout resource. The {@link ViewGroup} must not contain any other child views.
     * @param wrapperLayoutResource The layout resource specifying a wrapper {@link ViewGroup}.
     * @return A new adapter.
     */
    @CheckResult
    @NonNull
    public PowerAdapter wrapItems(@LayoutRes int wrapperLayoutResource) {
        return wrapItems(asViewFactory(wrapperLayoutResource));
    }

    /** Creates a composite adapter containing the items of all of the specified adapters in order. */
    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull PowerAdapter... adapters) {
        checkNotNull(adapters, "adapters");
        if (adapters.length == 0) {
            return EMPTY;
        }
        if (adapters.length == 1) {
            return adapters[0];
        }
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }

    /** Creates a composite adapter containing the items of all of the specified adapters in order. */
    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Collection<? extends PowerAdapter> adapters) {
        checkNotNull(adapters, "adapters");
        if (adapters.isEmpty()) {
            return EMPTY;
        }
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }

    /** Creates a composite adapter containing the items of all of the specified adapters in order. */
    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> adapters) {
        checkNotNull(adapters, "adapters");
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }

    /** Converts the specified fixed array of views to an adapter. */
    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull ViewFactory... views) {
        checkNotNull(views, "views");
        if (views.length == 0) {
            return EMPTY;
        }
        return new ItemAdapter(ItemAdapter.toItems(asList(views)));
    }

    /** Converts the specified fixed iterable of views to an adapter. */
    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull Iterable<? extends ViewFactory> views) {
        checkNotNull(views, "views");
        return new ItemAdapter(ItemAdapter.toItems(views));
    }

    /** Converts the specified fixed collection of views to an adapter. */
    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull Collection<? extends ViewFactory> views) {
        checkNotNull(views, "views");
        if (views.isEmpty()) {
            return EMPTY;
        }
        return new ItemAdapter(ItemAdapter.toItems(views));
    }

    /** Converts the specified fixed array of layout resources to an adapter. */
    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull @LayoutRes int... resources) {
        return new ItemAdapter(toItems(checkNotNull(resources, "resources")));
    }

    /** Represents an operation performed on a {@link PowerAdapter}. */
    public interface Transformer {
        @NonNull
        PowerAdapter transform(@NonNull PowerAdapter adapter);
    }
}
