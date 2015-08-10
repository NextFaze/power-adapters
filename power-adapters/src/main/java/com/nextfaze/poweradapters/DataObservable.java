package com.nextfaze.poweradapters;

import android.database.Observable;

final class DataObservable extends Observable<DataObserver> {

    final int size() {
        return mObservers.size();
    }

    final void notifyDataSetChanged() {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onChanged();
        }
    }

    final void notifyItemChanged(int position) {
        notifyItemRangeChanged(position, 1);
    }

    final void notifyItemRangeChanged(int positionStart, int itemCount) {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onItemRangeChanged(positionStart, itemCount);
        }
    }

    final void notifyItemInserted(int position) {
        notifyItemRangeInserted(position, 1);
    }

    final void notifyItemRangeInserted(int positionStart, int itemCount) {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
        }
    }

    final void notifyItemMoved(int fromPosition, int toPosition) {
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    final void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    final void notifyItemRemoved(int position) {
        notifyItemRangeRemoved(position, 1);
    }

    final void notifyItemRangeRemoved(int positionStart, int itemCount) {
        for (int i = mObservers.size() - 1; i >= 0; i--) {
            mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
        }
    }
}
