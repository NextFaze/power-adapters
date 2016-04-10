package com.nextfaze.poweradapters;

import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import lombok.NonNull;

import static com.nextfaze.poweradapters.PowerAdapters.concat;

public abstract class AbstractPowerAdapter implements PowerAdapter {

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    @NonNull
    private final DataObservable mDataObservable = new DataObservable();

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public long getItemId(int position) {
        return NO_ID;
    }

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        return mViewType;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public final void registerDataObserver(@NonNull DataObserver dataObserver) {
        boolean firstAdded;
        synchronized (mDataObservable) {
            mDataObservable.registerObserver(dataObserver);
            firstAdded = mDataObservable.size() == 1;
        }
        if (firstAdded) {
            onFirstObserverRegistered();
        }
    }

    @Override
    public final void unregisterDataObserver(@NonNull DataObserver dataObserver) {
        boolean lastRemoved;
        synchronized (mDataObservable) {
            mDataObservable.unregisterObserver(dataObserver);
            lastRemoved = mDataObservable.size() == 0;
        }
        if (lastRemoved) {
            onLastObserverUnregistered();
        }
    }

    /**
     * Notify any registered observers that the data set has changed.
     *
     * <p>There are two different classes of data change events, item changes and structural
     * changes. Item changes are when a single item has its data updated but no positional
     * changes have occurred. Structural changes are when items are inserted, removed or moved
     * within the data set.</p>
     *
     * <p>This event does not specify what about the data set has changed, forcing
     * any observers to assume that all existing items and structure may no longer be valid.
     * LayoutManagers will be forced to fully rebind and relayout all visible views.</p>
     *
     * <p><code>RecyclerView</code> will attempt to synthesize visible structural change events
     * for adapters that report that they have {@link #hasStableIds() stable IDs} when
     * this method is used. This can help for the purposes of animation and visual
     * object persistence but individual item views will still need to be rebound
     * and relaid out.</p>
     *
     * <p>If you are writing an adapter it will always be more efficient to use the more
     * specific change events if you can. Rely on <code>notifyDataSetChanged()</code>
     * as a last resort.</p>
     *
     * @see #notifyItemChanged(int)
     * @see #notifyItemInserted(int)
     * @see #notifyItemRemoved(int)
     * @see #notifyItemRangeChanged(int, int)
     * @see #notifyItemRangeInserted(int, int)
     * @see #notifyItemRangeRemoved(int, int)
     */
    public final void notifyDataSetChanged() {
        mDataObservable.notifyDataSetChanged();
    }

    /**
     * Notify any registered observers that the item at <code>position</code> has changed.
     *
     * <p>This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at <code>position</code> is out of date and should be updated.
     * The item at <code>position</code> retains the same identity.</p>
     *
     * @param position Position of the item that has changed
     *
     * @see #notifyItemRangeChanged(int, int)
     */
    public final void notifyItemChanged(int position) {
        mDataObservable.notifyItemChanged(position);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items starting at
     * position <code>positionStart</code> have changed.
     *
     * <p>This is an item change event, not a structural change event. It indicates that
     * any reflection of the data in the given position range is out of date and should
     * be updated. The items in the given range retain the same identity.</p>
     *
     * @param positionStart Position of the first item that has changed
     * @param itemCount Number of items that have changed
     *
     * @see #notifyItemChanged(int)
     */
    public final void notifyItemRangeChanged(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeChanged(positionStart, itemCount);
    }

    /**
     * Notify any registered observers that the item reflected at <code>position</code>
     * has been newly inserted. The item previously at <code>position</code> is now at
     * position <code>position + 1</code>.
     *
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     *
     * @param position Position of the newly inserted item in the data set
     *
     * @see #notifyItemRangeInserted(int, int)
     */
    public final void notifyItemInserted(int position) {
        mDataObservable.notifyItemInserted(position);
    }

    /**
     * Notify any registered observers that the currently reflected <code>itemCount</code>
     * items starting at <code>positionStart</code> have been newly inserted. The items
     * previously located at <code>positionStart</code> and beyond can now be found starting
     * at position <code>positionStart + itemCount</code>.
     *
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.</p>
     *
     * @param positionStart Position of the first item that was inserted
     * @param itemCount Number of items inserted
     *
     * @see #notifyItemInserted(int)
     */
    public final void notifyItemRangeInserted(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeInserted(positionStart, itemCount);
    }

    /**
     * Notify any registered observers that the item reflected at <code>fromPosition</code>
     * has been moved to <code>toPosition</code>.
     *
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     *
     * @param fromPosition Previous position of the item.
     * @param toPosition New position of the item.
     */
    public final void notifyItemMoved(int fromPosition, int toPosition) {
        mDataObservable.notifyItemMoved(fromPosition, toPosition);
    }

    public final void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        mDataObservable.notifyItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    /**
     * Notify any registered observers that the item previously located at <code>position</code>
     * has been removed from the data set. The items previously located at and after
     * <code>position</code> may now be found at <code>oldPosition - 1</code>.
     *
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.</p>
     *
     * @param position Position of the item that has now been removed
     *
     * @see #notifyItemRangeRemoved(int, int)
     */
    public final void notifyItemRemoved(int position) {
        mDataObservable.notifyItemRemoved(position);
    }

    /**
     * Notify any registered observers that the <code>itemCount</code> items previously
     * located at <code>positionStart</code> have been removed from the data set. The items
     * previously located at and after <code>positionStart + itemCount</code> may now be found
     * at <code>oldPosition - itemCount</code>.
     *
     * <p>This is a structural change event. Representations of other existing items in the data
     * set are still considered up to date and will not be rebound, though their positions
     * may be altered.</p>
     *
     * @param positionStart Previous position of the first item that was removed
     * @param itemCount Number of items removed from the data set
     */
    public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        mDataObservable.notifyItemRangeRemoved(positionStart, itemCount);
    }

    @CheckResult
    @NonNull
    @Override
    public final PowerAdapter decorate(@NonNull Decorator decorator) {
        return decorator.decorate(this);
    }

    @NonNull
    @Override
    public final PowerAdapter prepend(@NonNull PowerAdapter adapter) {
        return concat(adapter, this);
    }

    @CheckResult
    @NonNull
    @Override
    public final PowerAdapter append(@NonNull PowerAdapter adapter) {
        return concat(this, adapter);
    }

    @CheckResult
    @NonNull
    @Override
    public final PowerAdapter showOnlyWhile(@NonNull Condition condition) {
        return new ConditionalAdapter(this, condition);
    }

    /** Returns the number of registered observers. */
    protected final int getObserverCount() {
        return mDataObservable.size();
    }

    /** Called when the first observer has registered with this adapter. */
    @CallSuper
    protected void onFirstObserverRegistered() {
    }

    /** Called when the last observer has unregistered from this adapter. */
    @CallSuper
    protected void onLastObserverUnregistered() {
    }
}
