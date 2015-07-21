package com.nextfaze.databind.sample;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import com.nextfaze.databind.AvailableObserver;
import com.nextfaze.databind.Data;
import com.nextfaze.databind.ListAdapterWrapper;
import com.nextfaze.databind.LoadingObserver;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

@Accessors(prefix = "m")
public final class LoadNextAdapter extends ListAdapterWrapper {

    @NonNull
    private final Data<?> mData;

    @Getter
    @LayoutRes
    private final int mLoadNextItemResource;

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final AvailableObserver mAvailableObserver = new AvailableObserver() {
        @Override
        public void onAvailableChange() {
            notifyDataSetChanged();
        }
    };

    @Getter
    @Setter
    @Nullable
    private OnLoadNextClickListener mOnClickListener;

    public LoadNextAdapter(@NonNull Data<?> data, @NonNull ListAdapter adapter, @LayoutRes int loadNextItemResource) {
        super(adapter);
        mData = data;
        mLoadNextItemResource = loadNextItemResource;
        mData.registerLoadingObserver(mLoadingObserver);
        mData.registerAvailableObserver(mAvailableObserver);
    }

    @Override
    public void dispose() {
        super.dispose();
        mData.unregisterAvailableObserver(mAvailableObserver);
        mData.unregisterLoadingObserver(mLoadingObserver);
    }

    @Override
    public final int getCount() {
        if (isLoadNextShown()) {
            return super.getCount() + 1;
        }
        return super.getCount();
    }

    @Override
    public final boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isLoadNextItem(position)) {
            return false;
        }
        return super.isEnabled(position);
    }

    @Override
    public final long getItemId(int position) {
        if (isLoadNextItem(position)) {
            return -1;
        }
        return super.getItemId(position);
    }

    @Override
    public final Object getItem(int position) {
        if (isLoadNextItem(position)) {
            return null;
        }
        return super.getItem(position);
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public final int getItemViewType(int position) {
        if (isLoadNextItem(position)) {
            return getLoadNextItemViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (isLoadNextItem(position)) {
            if (convertView == null) {
                convertView = newLoadNextView(parent);
            }
            return convertView;
        }
        return super.getView(position, convertView, parent);
    }

    public void loadNext() {
        dispatchClick();
    }

    private void dispatchClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick();
        }
    }

    private boolean isLoadNextShown() {
        return !mData.isLoading() && !mData.isEmpty() && mData.available() > 0;
    }

    private boolean isLoadNextItem(int position) {
        return position == super.getCount();
    }

    private int getLoadNextItemViewType() {
        return super.getViewTypeCount();
    }

    @NonNull
    private View newLoadNextView(@NonNull ViewGroup parent) {
        View v = LayoutInflater.from(parent.getContext()).inflate(mLoadNextItemResource, parent, false);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNext();
            }
        });
        return v;
    }

    public interface OnLoadNextClickListener {
        void onClick();
    }
}
