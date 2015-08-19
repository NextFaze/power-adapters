package com.nextfaze.poweradapters.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewType;
import lombok.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class RecyclerConverterAdapter extends RecyclerView.Adapter<RecyclerConverterAdapter.Holder> {

    @NonNull
    private final Set<RecyclerView.AdapterDataObserver> mAdapterDataObservers = new HashSet<>();

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

    @NonNull
    private final Map<ViewType, Integer> mViewTypeObjectToInt = new HashMap<>();

    @NonNull
    private final Map<Integer, ViewType> mViewTypeIntToObject = new HashMap<>();

    private int mNextViewTypeInt;

    RecyclerConverterAdapter(@NonNull PowerAdapter powerAdapter) {
        mPowerAdapter = powerAdapter;
        setHasStableIds(mPowerAdapter.hasStableIds());
    }

    @Override
    public int getItemCount() {
        return mPowerAdapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        return mPowerAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        ViewType viewType = mPowerAdapter.getItemViewType(position);
        Integer viewTypeInt = mViewTypeObjectToInt.get(viewType);
        if (viewTypeInt == null) {
            viewTypeInt = mNextViewTypeInt++;
            mViewTypeObjectToInt.put(viewType, viewTypeInt);
            mViewTypeIntToObject.put(viewTypeInt, viewType);
        }
        return viewTypeInt;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int itemViewType) {
        return new Holder(mPowerAdapter.newView(parent, mViewTypeIntToObject.get(itemViewType)));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        mPowerAdapter.bindView(holder.itemView, holder.holder);
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
