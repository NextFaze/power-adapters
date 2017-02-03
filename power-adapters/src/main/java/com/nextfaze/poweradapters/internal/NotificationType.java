package com.nextfaze.poweradapters.internal;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public enum NotificationType {
    /** Only use {@link DataObservable#notifyDataSetChanged()}. */
    COARSE {
        @Override
        public void notifyItemChanged(@NonNull DataObservable observable, int position) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemRangeChanged(@NonNull DataObservable observable, int positionStart, int itemCount) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemInserted(@NonNull DataObservable observable, int position) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemRangeInserted(@NonNull DataObservable observable, int positionStart, int itemCount) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemMoved(@NonNull DataObservable observable, int fromPosition, int toPosition) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemRangeMoved(@NonNull DataObservable observable,
                                         int fromPosition,
                                         int toPosition,
                                         int itemCount) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemRemoved(@NonNull DataObservable observable, int position) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemRangeRemoved(@NonNull DataObservable observable, int positionStart, int itemCount) {
            notifyDataSetChanged(observable);
        }
    },
    /** Use all fine-grained notification methods on {@link DataObservable}. */
    FINE;

    public void notifyDataSetChanged(@NonNull DataObservable observable) {
        observable.notifyDataSetChanged();
    }

    public void notifyItemChanged(@NonNull DataObservable observable, int position) {
        observable.notifyItemChanged(position);
    }

    public void notifyItemRangeChanged(@NonNull DataObservable observable, int positionStart, int itemCount) {
        observable.notifyItemRangeChanged(positionStart, itemCount);
    }

    public void notifyItemInserted(@NonNull DataObservable observable, int position) {
        observable.notifyItemInserted(position);
    }

    public void notifyItemRangeInserted(@NonNull DataObservable observable, int positionStart, int itemCount) {
        observable.notifyItemRangeInserted(positionStart, itemCount);
    }

    public void notifyItemMoved(@NonNull DataObservable observable, int fromPosition, int toPosition) {
        observable.notifyItemMoved(fromPosition, toPosition);
    }

    public void notifyItemRangeMoved(@NonNull DataObservable observable, int fromPosition, int toPosition, int itemCount) {
        observable.notifyItemRangeMoved(fromPosition, toPosition, itemCount);
    }

    public void notifyItemRemoved(@NonNull DataObservable observable, int position) {
        observable.notifyItemRemoved(position);
    }

    public void notifyItemRangeRemoved(@NonNull DataObservable observable, int positionStart, int itemCount) {
        observable.notifyItemRangeRemoved(positionStart, itemCount);
    }
}
