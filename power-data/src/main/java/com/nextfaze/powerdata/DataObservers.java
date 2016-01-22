package com.nextfaze.powerdata;

final class DataObservers extends Observers<DataObserver> {
    void notifyDataChanged() {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onChange();
        }
    }

    void notifyItemChanged(int position) {
        notifyItemRangeChanged(position, 1);
    }

    void notifyItemRangeChanged(int positionStart, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeChanged(positionStart, itemCount);
        }
    }

    void notifyItemInserted(int position) {
        notifyItemRangeInserted(position, 1);
    }

    void notifyItemRangeInserted(int positionStart, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeInserted(positionStart, itemCount);
        }
    }

    void notifyItemMoved(int fromPosition, int toPosition) {
        notifyItemRangeMoved(fromPosition, toPosition, 1);
    }

    void notifyItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    }

    void notifyItemRemoved(int position) {
        notifyItemRangeRemoved(position, 1);
    }

    void notifyItemRangeRemoved(int positionStart, int itemCount) {
        for (DataObserver dataObserver : mObservers) {
            dataObserver.onItemRangeRemoved(positionStart, itemCount);
        }
    }
}
