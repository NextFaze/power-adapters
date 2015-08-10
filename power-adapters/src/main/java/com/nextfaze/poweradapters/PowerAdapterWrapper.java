package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class PowerAdapterWrapper extends AbstractPowerAdapter {

    @NonNull
    private final Set<DataObserver> mDataSetObservers = new CopyOnWriteArraySet<>();

    @NonNull
    private final PowerAdapter mAdapter;

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
            notifyItemRangeMoved(fromPosition, toPosition, itemCount);
        }
    };

    public PowerAdapterWrapper(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
    }

    @NonNull
    public final PowerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public boolean hasStableIds() {
        return mAdapter.hasStableIds();
    }

    @Override
    public int getViewTypeCount() {
        return mAdapter.getViewTypeCount();
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    @NonNull
    public Metadata getItemMetadata(int position) {
        return mAdapter.getItemMetadata(position);
    }

    @Override
    @NonNull
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        return mAdapter.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, int position) {
        mAdapter.bindView(view, position);
    }
}
