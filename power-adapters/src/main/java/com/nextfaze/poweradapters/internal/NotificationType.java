package com.nextfaze.poweradapters.internal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public enum NotificationType {
    /** Only use {@link DataObservable#notifyDataSetChanged()}. */
    COARSE {
        @Override
        public void notifyItemChanged(@NonNull DataObservable observable, int position, @Nullable Object payload) {
            notifyDataSetChanged(observable);
        }

        @Override
        public void notifyItemRangeChanged(@NonNull DataObservable observable, int positionStart, int itemCount, @Nullable Object payload) {
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

    public void notifyItemChanged(@NonNull DataObservable observable, int position, @Nullable Object payload) {
        observable.notifyItemChanged(position, payload);
    }

    public void notifyItemRangeChanged(@NonNull DataObservable observable, int positionStart, int itemCount, @Nullable Object payload) {
        observable.notifyItemRangeChanged(positionStart, itemCount, payload);
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
