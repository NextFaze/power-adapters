package com.nextfaze.powerdata.recyclerview;

import android.support.v7.widget.RecyclerView;
import com.nextfaze.powerdata.Data;
import com.nextfaze.powerdata.DataObserver;
import lombok.NonNull;

import java.util.HashSet;
import java.util.Set;

public abstract class RecyclerDataAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final Set<RecyclerView.AdapterDataObserver> mDataObservers = new HashSet<>();

    @NonNull
    private final DataObserver mDataObserver = new DataObserver() {
        @Override
        public void onChange() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (itemCount == 1) {
                notifyItemMoved(fromPosition, toPosition);
            } else {
                notifyDataSetChanged();
            }
        }
    };

    protected RecyclerDataAdapter(@NonNull Data<?> data) {
        mData = data;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        if (mDataObservers.add(observer) && mDataObservers.size() == 1) {
            mData.registerDataObserver(mDataObserver);
        }
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        if (mDataObservers.remove(observer) && mDataObservers.size() == 0) {
            mData.unregisterDataObserver(mDataObserver);
        }
    }
}
