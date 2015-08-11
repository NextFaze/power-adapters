package com.nextfaze.poweradapters.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.PowerAdapter;
import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

final class RecyclerConverterAdapter extends RecyclerView.Adapter<RecyclerConverterAdapter.Holder> {

    @NonNull
    private final Set<RecyclerView.AdapterDataObserver> mAdapterDataObservers = new CopyOnWriteArraySet<>();

    @NonNull
    private final PowerAdapter mPowerAdapter;

    @NonNull
    private final DataObserver mDataSetObserver = new DataObserver() {
        @Override
        public void onChanged() {
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
                // TODO: There's likely a more specific (series of?) calls we can make here instead of generic "changed" fallback.
                notifyDataSetChanged();
            }
        }
    };

    RecyclerConverterAdapter(@NonNull PowerAdapter powerAdapter) {
        mPowerAdapter = powerAdapter;
        setHasStableIds(mPowerAdapter.hasStableIds());
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        return new Holder(mPowerAdapter.newView(parent, itemViewType));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        mPowerAdapter.bindView(holder.itemView, holder.holder);
    }

    @Override
    public int getItemCount() {
        return mPowerAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mPowerAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return mPowerAdapter.getItemId(position);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        if (mAdapterDataObservers.add(observer) && mAdapterDataObservers.size() == 1) {
            mPowerAdapter.registerDataObserver(mDataSetObserver);
        }
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.unregisterAdapterDataObserver(observer);
        if (mAdapterDataObservers.remove(observer) && mAdapterDataObservers.size() == 0) {
            mPowerAdapter.unregisterDataObserver(mDataSetObserver);
        }
    }

    final class Holder extends RecyclerView.ViewHolder {

        @NonNull
        private final com.nextfaze.poweradapters.Holder holder = new com.nextfaze.poweradapters.Holder() {
            @Override
            public int getPosition() {
                return getAdapterPosition();
            }
        };

        Holder(View itemView) {
            super(itemView);
        }
    }
}
