package com.nextfaze.poweradapters.rxjava2.internal;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.v7.util.ListUpdateCallback;
import com.nextfaze.poweradapters.internal.DataObservable;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
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
        mDataObservable.notifyItemRangeChanged(position, count);
    }
}
