package com.nextfaze.poweradapters.sample;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.asyncdata.AvailableObserver;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.asyncdata.DataObserver;
import com.nextfaze.asyncdata.LoadingObserver;
import com.nextfaze.asyncdata.SimpleDataObserver;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.PowerAdapterWrapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

@Accessors(prefix = "m")
final class LoadNextAdapter extends PowerAdapterWrapper {

    @NonNull
    private final Data<?> mData;

    @Getter
    @LayoutRes
    private final int mLoadNextItemResource;

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChange() {
            updateVisible();
        }
    };

    @NonNull
    private final LoadingObserver mLoadingObserver = new LoadingObserver() {
        @Override
        public void onLoadingChange() {
            updateVisible();
        }
    };

    @NonNull
    private final AvailableObserver mAvailableObserver = new AvailableObserver() {
        @Override
        public void onAvailableChange() {
            updateVisible();
        }
    };

    @Getter
    @Setter
    @Nullable
    private OnLoadNextClickListener mOnClickListener;

    private boolean mVisible;

    LoadNextAdapter(@NonNull Data<?> data, @NonNull PowerAdapter adapter, @LayoutRes int loadNextItemResource) {
        super(adapter);
        mData = data;
        mLoadNextItemResource = loadNextItemResource;
        updateVisible();
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mData.registerDataObserver(mDataObserver);
        mData.registerLoadingObserver(mLoadingObserver);
        mData.registerAvailableObserver(mAvailableObserver);
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mData.unregisterDataObserver(mDataObserver);
        mData.unregisterAvailableObserver(mAvailableObserver);
        mData.unregisterLoadingObserver(mLoadingObserver);
    }

    @Override
    public int getItemCount() {
        if (mVisible) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        if (isLoadNextItem(position)) {
            return NO_ID;
        }
        return super.getItemId(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadNextItem(position)) {
            return loadNextItemViewType();
        }
        return super.getItemViewType(position);
    }

    @Override
    public boolean isEnabled(int position) {
        //noinspection SimplifiableIfStatement
        if (isLoadNextItem(position)) {
            return true;
        }
        return super.isEnabled(position);
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, int itemViewType) {
        if (itemViewType == loadNextItemViewType()) {
            return newLoadNextView(parent);
        }
        return super.newView(parent, itemViewType);
    }

    @Override
    public void bindView(@NonNull View view, @NonNull Holder holder) {
        if (!isLoadNextItem(holder.getPosition())) {
            super.bindView(view, holder);
        }
    }

    @Override
    protected int outerToInner(int outerPosition) {
        // No conversion necessary, as loading item appears at the end.
        return outerPosition;
    }

    @Override
    protected int innerToOuter(int innerPosition) {
        // No conversion necessary, as loading item appears at the end.
        return super.innerToOuter(innerPosition);
    }

    void loadNext() {
        dispatchClick();
    }

    private void dispatchClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick();
        }
    }

    private boolean isLoadNextVisible() {
        return mLoadNextItemResource > 0 && !mData.isLoading() && !mData.isEmpty() && mData.available() > 0;
    }

    private void updateVisible() {
        boolean visible = isLoadNextVisible();
        if (visible != mVisible) {
            mVisible = visible;
            if (visible) {
                notifyItemInserted(super.getItemCount());
            } else {
                notifyItemRemoved(super.getItemCount());
            }
        }
    }

    private boolean isLoadNextItem(int position) {
        return position == super.getItemCount();
    }

    private int loadNextItemViewType() {
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

    interface OnLoadNextClickListener {
        void onClick();
    }
}
