package com.nextfaze.databind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;

/**
 * Wraps an existing {@link ListAdapter} and displays a loading indicator while the supplied {@link Data} is in the
 * loading state.
 */
public class LoadingAdapter extends ListAdapterWrapper {

    @NonNull
    private final Data<?> mData;

    private final int mLoadingItemResourceId;

    public LoadingAdapter(@NonNull Data<?> data, @NonNull ListAdapter adapter, int loadingItemResourceId) {
        super(adapter);
        mLoadingItemResourceId = loadingItemResourceId;
        mData = data;
        mData.registerLoadingObserver(new LoadingObserver() {
            @Override
            public void onLoadingChange() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        if (shouldShowLoadingItem()) {
            return mAdapter.getCount() + 1;
        }
        return mAdapter.getCount();
    }

    @Override
    public int getViewTypeCount() {
        // Fixed amount of view types: whatever the underlying adapter wants, plus our loading item.
        return super.getViewTypeCount() + 1;
    }

    private int getLoadingViewType() {
        return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (shouldShowLoadingItem() && isLoadingItem(position)) {
            return getLoadingViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == getLoadingViewType()) {
            if (convertView == null) {
                convertView = newLoadingView(parent);
            }
            return convertView;
        }
        return super.getView(position, convertView, parent);
    }

    @NonNull
    protected View newLoadingView(@NonNull ViewGroup parent) {
        return getLayoutInflater(parent).inflate(mLoadingItemResourceId, parent, false);
    }

    private boolean shouldShowLoadingItem() {
        return mData.isLoading();
    }

    private boolean isLoadingItem(int position) {
        // Last item in the list.
        return position == getCount() - 1;
    }

    @NonNull
    private LayoutInflater getLayoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}