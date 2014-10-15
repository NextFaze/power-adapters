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
public abstract class LoadingAdapter extends ListAdapterWrapper {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            notifyDataSetChanged();
        }
    };

    @Getter
    @Setter
    private boolean mLoadingItemEnabled;

    @NonNull
    public static LoadingAdapter create(@NonNull Data<?> data, @NonNull ListAdapter adapter, final int loadingItemResource) {
        return new LoadingAdapter(data, adapter) {
            @NonNull
            @Override
            protected View newLoadingView(@NonNull LayoutInflater layoutInflater,
                                          int position,
                                          @NonNull ViewGroup parent) {
                return layoutInflater.inflate(loadingItemResource, parent, false);
            }
        };
    }

    @NonNull
    public static LoadingAdapter create(@NonNull Data<?> data, @NonNull ListAdapter adapter, @NonNull final View loadingView) {
        return new LoadingAdapter(data, adapter) {
            @NonNull
            @Override
            protected View newLoadingView(@NonNull LayoutInflater layoutInflater,
                                          int position,
                                          @NonNull ViewGroup parent) {
                return loadingView;
            }
        };
    }

    public LoadingAdapter(@NonNull Data<?> data, @NonNull ListAdapter adapter) {
        super(adapter);
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
                convertView = newLoadingView(getLayoutInflater(parent), position, parent);
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
    protected abstract View newLoadingView(@NonNull LayoutInflater layoutInflater,
                                           int position,
                                           @NonNull ViewGroup parent);

    private boolean shouldShowLoadingItem() {
        return mData.isLoading();
    }

    private boolean isLoadingItem(int position) {
        // Last item in the list.
        return position == getCount() - 1;
    }

    @NonNull
    private static LayoutInflater getLayoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}