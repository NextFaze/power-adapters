package com.nextfaze.databind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Wraps an existing {@link ListAdapter} and displays a loading indicator while the supplied {@link Data} is in the
 * loading state.
 */
@Accessors(prefix = "m")
public class LoadingAdapter extends ListAdapterWrapper {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            notifyDataSetChanged();
        }
    };

    private final int mLoadingItemResource;

    @Getter
    @Setter
    private boolean mLoadingItemEnabled;

    public LoadingAdapter(@NonNull Data<?> data, @NonNull ListAdapter adapter, int loadingItemResource) {
        super(adapter);
        mLoadingItemResource = loadingItemResource;
        mData = data;
        mData.registerLoadingObserver(mLoadingObserver);
    }

    @Override
    public void dispose() {
        super.dispose();
        mData.unregisterLoadingObserver(mLoadingObserver);
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

    @Override
    public boolean isEnabled(int position) {
        if (shouldShowLoadingItem() && isLoadingItem(position)) {
            return mLoadingItemEnabled;
        }
        return super.isEnabled(position);
    }

    @NonNull
    protected View newLoadingView(@NonNull ViewGroup parent) {
        return getLayoutInflater(parent).inflate(mLoadingItemResource, parent, false);
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