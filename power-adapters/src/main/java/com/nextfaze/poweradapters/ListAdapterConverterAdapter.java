package com.nextfaze.poweradapters;

import android.database.DataSetObserver;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.os.Looper.getMainLooper;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

final class ListAdapterConverterAdapter extends BaseAdapter {

    @NonNull
    private final WeakMap<ViewGroup, ContainerImpl> mParentViewToContainer = new WeakMap<>();

    @NonNull
    final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final WeakMap<View, HolderImpl> mHolders = new WeakMap<>();

    @NonNull
    private final Map<Object, Integer> mViewTypeObjectToInt = new HashMap<>();

    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new HashSet<>();

    @NonNull
    final Runnable mNotifyDataSetChangedRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            // AdapterView will act on this notification immediately, so we use the following risky technique to ensure
            // possible subsequent notifications are fully executed before it does so.
            // This ensures it doesn't try to access ranges of this PowerAdapter that may be in a dirty state, such as
            // children of ConcatAdapter.
            mHandler.removeCallbacks(mNotifyDataSetChangedRunnable);
            mHandler.postAtFrontOfQueue(mNotifyDataSetChangedRunnable);
        }
    };

    @NonNull
    final PowerAdapter mPowerAdapter;

    private final int mViewTypeCount;

    private int mNextViewTypeInt;

    ListAdapterConverterAdapter(@NonNull PowerAdapter powerAdapter, int viewTypeCount) {
        if (viewTypeCount < 1) {
            throw new IllegalArgumentException("viewTypeCount must be at least 1");
        }
        mPowerAdapter = checkNotNull(powerAdapter, "powerAdapter");
        mViewTypeCount = viewTypeCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return mPowerAdapter.getItemCount();
    }

    @Override
    public long getItemId(int position) {
        long itemId = mPowerAdapter.getItemId(position);
        // AdapterViews require this when items don't have a proper stable ID. Otherwise, the scroll position is not
        // retained between config changes.
        if (itemId == PowerAdapter.NO_ID) {
            return position;
        }
        return itemId;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ContainerImpl container = mParentViewToContainer.get(parent);
        if (container == null) {
            container = new ContainerImpl(parent);
            mParentViewToContainer.put(parent, container);
        }
        if (convertView == null) {
            convertView = mPowerAdapter.newView(parent, mPowerAdapter.getItemViewType(position));
        }
        HolderImpl holder = mHolders.get(convertView);
        if (holder == null) {
            holder = new HolderImpl();
            mHolders.put(convertView, holder);
        }
        holder.position = position;
        mPowerAdapter.bindView(container, convertView, holder);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return mPowerAdapter.hasStableIds();
    }

    @Override
    public int getItemViewType(int position) {
        Object viewType = mPowerAdapter.getItemViewType(position);
        Integer viewTypeInt = mViewTypeObjectToInt.get(viewType);
        if (viewTypeInt == null) {
            viewTypeInt = mNextViewTypeInt++;
            mViewTypeObjectToInt.put(viewType, viewTypeInt);
        }
        return viewTypeInt;
    }

    @Override
    public int getViewTypeCount() {
        return mViewTypeCount;
    }

    @Override
    public boolean isEnabled(int position) {
        return mPowerAdapter.isEnabled(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataSetObservers.add(observer) && mDataSetObservers.size() == 1) {
            mPowerAdapter.registerDataObserver(mDataObserver);
        }
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataSetObservers.remove(observer) && mDataSetObservers.size() == 0) {
            mPowerAdapter.unregisterDataObserver(mDataObserver);
        }
    }

    private static final class HolderImpl implements Holder {

        int position;

        HolderImpl() {
        }

        @Override
        public int getPosition() {
            return position;
        }
    }

    private final class ContainerImpl extends Container {

        @NonNull
        private final ViewGroup mParentView;

        ContainerImpl(@NonNull ViewGroup parentView) {
            mParentView = parentView;
        }

        @Override
        public void scrollToPosition(int position) {
            if (mParentView instanceof AbsListView) {
                ((AbsListView) mParentView).smoothScrollToPosition(position);
            }
        }

        @Override
        public int getItemCount() {
            return mPowerAdapter.getItemCount();
        }

        @NonNull
        @Override
        public ViewGroup getViewGroup() {
            return mParentView;
        }

        @NonNull
        @Override
        public Container getRootContainer() {
            return this;
        }
    }
}
