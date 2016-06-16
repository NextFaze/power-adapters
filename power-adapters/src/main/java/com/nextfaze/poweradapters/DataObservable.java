package com.nextfaze.poweradapters;

import lombok.NonNull;

import java.util.ArrayList;

/** @hide Not intended for public use. */
public final class DataObservable {

    @NonNull
    private final ArrayList<DataObserver> mObservers = new ArrayList<>();

    public void registerObserver(@NonNull DataObserver observer) {
        if (mObservers.contains(observer)) {
            throw new IllegalStateException("Observer is already registered.");
        }
        mObservers.add(observer);
    }

    public void unregisterObserver(@NonNull DataObserver observer) {
        int index = mObservers.indexOf(observer);
        if (index == -1) {
            throw new IllegalStateException("Observer was not registered.");
        }
        mObservers.remove(index);
    }

    public int getObserverCount() {
        return mObservers.size();
    }

    /**
     * Notify any registered observers that the data set has changed.
     * <p>
     * This event does not specify what about the data set has changed, forcing
     * any observers to assume that all existing items and structure may no longer be valid.
     * <p>
     * @see #notifyItemChanged(int)
     * @see #notifyItemInserted(int)
     * @see #notifyItemRemoved(int)
     * @see #notifyItemRangeChanged(int, int)
     * @see #notifyItemRangeInserted(int, int)
     * @see #notifyItemRangeRemoved(int, int)
     */
    public void notifyDataSetChanged() {
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
    public void notifyItemChanged(int position) {
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
    public void notifyItemRangeChanged(int positionStart, int itemCount) {
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
    public void notifyItemInserted(int position) {
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
    public void notifyItemRangeInserted(int positionStart, int itemCount) {
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
    public void notifyItemMoved(int fromPosition, int toPosition) {
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    public void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
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
    public void notifyItemRemoved(int position) {
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
    public void notifyItemRangeRemoved(int positionStart, int itemCount) {
        if (itemCount > 0) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
            }
        }
    }
}
