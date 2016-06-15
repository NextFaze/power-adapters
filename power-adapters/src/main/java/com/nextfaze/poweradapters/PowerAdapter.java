package com.nextfaze.poweradapters;

import android.annotation.SuppressLint;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;

import static com.nextfaze.poweradapters.ItemAdapter.toItems;
import static java.util.Arrays.asList;

@SuppressWarnings("WeakerAccess")
public abstract class PowerAdapter {

    /** An adapter with no elements. */
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
        public ViewType getItemViewType(int position) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEnabled(int position) {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void bindView(@NonNull View view, @NonNull Holder holder) {
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

    public static final int NO_ID = -1;

    @NonNull
    private final ArrayList<DataObserver> mObservers = new ArrayList<>();

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
     * Get the type of view for item at {@code position}, for the purpose of view recycling.
     * @param position The position of the item within the adapter's data set whose view type we
     * want.
     * @return A {@link ViewType} object that is unique for this type of view throughout the process.
     */
    @NonNull
    public abstract ViewType getItemViewType(int position);

    /**
     * Returns true if the item at the specified position is not a separator.
     * (A separator is a non-selectable, non-clickable item).
     * The result is unspecified if position is invalid. An {@link ArrayIndexOutOfBoundsException}
     * should be thrown in that case for fast failure.
     * @param position Index of the item
     * @return True if the item is not a separator
     */
    public boolean isEnabled(int position) {
        return true;
    }

    @NonNull
    public abstract View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType);

    public abstract void bindView(@NonNull View view, @NonNull Holder holder);

    @CallSuper
    public void registerDataObserver(@NonNull DataObserver dataObserver) {
        if (mObservers.contains(dataObserver)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(dataObserver);
        if (mObservers.size() == 1) {
            onFirstObserverRegistered();
        }
    }

    @CallSuper
    public void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        int index = mObservers.indexOf(dataObserver);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
        if (mObservers.size() == 0) {
            onLastObserverUnregistered();
        }
    }

    /** Returns the number of registered observers. */
    protected final int getObserverCount() {
        return mObservers.size();
    }

    /** Called when the first observer has registered with this adapter. */
    @CallSuper
    protected void onFirstObserverRegistered() {
    }

    /** Called when the last observer has unregistered from this adapter. */
    @CallSuper
    protected void onLastObserverUnregistered() {
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
     * LayoutManagers will be forced to fully rebind and relayout all visible views.</p>
     * <p>
     * <p><code>RecyclerView</code> will attempt to synthesize visible structural change events
     * for adapters that report that they have {@link #hasStableIds() stable IDs} when
     * this method is used. This can help for the purposes of animation and visual
     * object persistence but individual item views will still need to be rebound
     * and relaid out.</p>
     * <p>
     * <p>If you are writing an adapter it will always be more efficient to use the more
     * specific change events if you can. Rely on <code>notifyDataSetChanged()</code>
     * as a last resort.</p>
     * @see #notifyItemChanged(int)
     * @see #notifyItemInserted(int)
     * @see #notifyItemRemoved(int)
     * @see #notifyItemRangeChanged(int, int)
     * @see #notifyItemRangeInserted(int, int)
     * @see #notifyItemRangeRemoved(int, int)
     */
    protected final void notifyDataSetChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
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
        notifyItemRangeChanged(position, 1);
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
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeChanged(positionStart, itemCount);
            }
        }
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
        notifyItemRangeInserted(position, 1);
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
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
            }
        }
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
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    protected final void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, itemCount);
            }
        }
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
        notifyItemRangeRemoved(position, 1);
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
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
            }
        }
    }

    /** Applies the specified {@link Transformer} to this adapter, and returns the result. */
    @CheckResult
    @NonNull
    public final PowerAdapter compose(@NonNull Transformer transformer) {
        return transformer.transform(this);
    }

    @CheckResult
    @NonNull
    public final PowerAdapter prepend(@NonNull PowerAdapter... adapters) {
        if (adapters.length == 0) {
            return this;
        }
        return new ConcatAdapterBuilder().addAll(adapters).add(this).build();
    }

    @CheckResult
    @NonNull
    public final PowerAdapter prepend(@NonNull ViewFactory... views) {
        if (views.length == 0) {
            return this;
        }
        return prepend(asAdapter(views));
    }

    @CheckResult
    @NonNull
    public final PowerAdapter prepend(@NonNull @LayoutRes int... layoutResources) {
        if (layoutResources.length == 0) {
            return this;
        }
        return prepend(asAdapter(layoutResources));
    }

    @CheckResult
    @NonNull
    public final PowerAdapter append(@NonNull PowerAdapter... adapters) {
        if (adapters.length == 0) {
            return this;
        }
        return new ConcatAdapterBuilder().add(this).addAll(adapters).build();
    }

    @CheckResult
    @NonNull
    public final PowerAdapter append(@NonNull ViewFactory... views) {
        if (views.length == 0) {
            return this;
        }
        return append(asAdapter(views));
    }

    @CheckResult
    @NonNull
    public final PowerAdapter append(@NonNull @LayoutRes int... layoutResources) {
        if (layoutResources.length == 0) {
            return this;
        }
        return append(asAdapter(layoutResources));
    }

    @CheckResult
    @NonNull
    public final PowerAdapter offset(int offset) {
        if (offset <= 0) {
            return this;
        }
        return new OffsetAdapter(this, offset);
    }

    @CheckResult
    @NonNull
    public final PowerAdapter limit(int limit) {
        if (limit == Integer.MAX_VALUE) {
            return this;
        }
        return new LimitAdapter(this, limit);
    }

    @CheckResult
    @NonNull
    public final PowerAdapter showOnlyWhile(@NonNull Condition condition) {
        if (condition instanceof ConstantCondition) {
            if (condition.eval()) {
                return this;
            } else {
                return EMPTY;
            }
        }
        return new ConditionalAdapter(this, condition);
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull PowerAdapter... adapters) {
        if (adapters.length == 0) {
            return EMPTY;
        }
        if (adapters.length == 1) {
            return adapters[0];
        }
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Collection<? extends PowerAdapter> adapters) {
        if (adapters.isEmpty()) {
            return EMPTY;
        }
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }

    @CheckResult
    @NonNull
    public static PowerAdapter concat(@NonNull Iterable<? extends PowerAdapter> adapters) {
        return new ConcatAdapterBuilder().addAll(adapters).build();
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull ViewFactory... views) {
        if (views.length == 0) {
            return EMPTY;
        }
        return new ItemAdapter(ItemAdapter.toItems(asList(views)));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull Iterable<? extends ViewFactory> views) {
        return new ItemAdapter(ItemAdapter.toItems(views));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull Collection<? extends ViewFactory> views) {
        if (views.isEmpty()) {
            return EMPTY;
        }
        return new ItemAdapter(ItemAdapter.toItems(views));
    }

    @CheckResult
    @NonNull
    public static PowerAdapter asAdapter(@NonNull @LayoutRes int... resources) {
        return new ItemAdapter(toItems(resources));
    }

    public interface Transformer {
        @NonNull
        PowerAdapter transform(@NonNull PowerAdapter adapter);
    }
}
