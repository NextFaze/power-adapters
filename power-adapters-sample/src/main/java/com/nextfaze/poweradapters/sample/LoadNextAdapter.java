package com.nextfaze.poweradapters.sample;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.PowerAdapterWrapper;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.ViewType;
import com.nextfaze.poweradapters.ViewTypes;
import com.nextfaze.poweradapters.data.AvailableObserver;
import com.nextfaze.poweradapters.data.Data;
import com.nextfaze.poweradapters.data.DataObserver;
import com.nextfaze.poweradapters.data.LoadingObserver;
import com.nextfaze.poweradapters.data.SimpleDataObserver;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
final class LoadNextAdapter extends PowerAdapterWrapper {

    @NonNull
    private final Data<?> mData;

    @Getter
    @NonNull
    private final ViewFactory mLoadNextViewFactory;

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

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    @Getter
    @Setter
    @Nullable
    private OnLoadNextClickListener mOnClickListener;

    private boolean mVisible;

    LoadNextAdapter(@NonNull PowerAdapter adapter, @NonNull Data<?> data, @NonNull ViewFactory loadNextViewFactory) {
        super(adapter);
        mData = data;
        mLoadNextViewFactory = loadNextViewFactory;
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

    @NonNull
    @Override
    public ViewType getItemViewType(int position) {
        if (isLoadNextItem(position)) {
            return mViewType;
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
    public View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        if (viewType == mViewType) {
            return newLoadNextView(parent);
        }
        return super.newView(parent, viewType);
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
        return !mData.isLoading() && !mData.isEmpty() && mData.available() > 0;
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

    @NonNull
    private View newLoadNextView(@NonNull ViewGroup parent) {
        View v = mLoadNextViewFactory.create(parent);
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
