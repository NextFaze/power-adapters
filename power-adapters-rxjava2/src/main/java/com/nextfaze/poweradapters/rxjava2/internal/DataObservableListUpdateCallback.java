package com.nextfaze.poweradapters.rxjava2.internal;

import com.nextfaze.poweradapters.internal.DataObservable;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.recyclerview.widget.ListUpdateCallback;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/** For internal use only. */
@RestrictTo(LIBRARY_GROUP)
final class DataObservableListUpdateCallback implements ListUpdateCallback {

    @NonNull
    private final DataObservable mDataObservable;

    public DataObservableListUpdateCallback(@NonNull DataObservable dataObservable) {
        mDataObservable = checkNotNull(dataObservable, "dataObservable");
    }

    @Override
    public void onInserted(int position, int count) {
        mDataObservable.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        mDataObservable.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        mDataObservable.notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onChanged(int position, int count, Object payload) {
        mDataObservable.notifyItemRangeChanged(position, count, payload);
    }
}
