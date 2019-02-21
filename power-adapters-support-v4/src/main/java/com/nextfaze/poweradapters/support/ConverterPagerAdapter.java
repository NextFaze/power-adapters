package com.nextfaze.poweradapters.support;

import android.database.DataSetObserver;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapterFixed;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.SimpleDataObserver;
import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public class ConverterPagerAdapter extends PagerAdapterFixed {

    @NonNull
    private final WeakMap<ViewGroup, ViewPagerContainer> mParentViewToContainer = new WeakMap<>();

    @NonNull
    private final Recycler mRecycler = new Recycler();

    @NonNull
    private final DataObserver mDataObserver = new SimpleDataObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }
    };

    @NonNull
    private final WeakMap<View, Object> mViewTypes = new WeakMap<>();

    @NonNull
    private final WeakMap<View, HolderImpl> mHolders = new WeakMap<>();

    /** Keep track of observers registered, so we know when to register our own. */
    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new HashSet<>();

    @NonNull
    public final PowerAdapter mAdapter;

    public ConverterPagerAdapter(@NonNull PowerAdapter adapter) {
        mAdapter = checkNotNull(adapter, "adapter");
    }

    @CallSuper
    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        super.registerDataSetObserver(observer);
        if (mDataSetObservers.add(observer) && mDataSetObservers.size() == 1) {
            mAdapter.registerDataObserver(mDataObserver);
        }
    }

    @CallSuper
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        super.unregisterDataSetObserver(observer);
        if (mDataSetObservers.remove(observer) && mDataSetObservers.size() == 0) {
            mAdapter.unregisterDataObserver(mDataObserver);
        }
    }

    @Override
    public final int getCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public int getItemPosition(Object object) {
        // Required for notifyDataSetChanged() to have any effect.
        return POSITION_NONE;
    }

    @Override
    public final Object instantiateItem(ViewGroup parent, int position) {
        ViewPagerContainer container = mParentViewToContainer.get(parent);
        if (container == null) {
            container = new ViewPagerContainer((ViewPager) parent);
            mParentViewToContainer.put(parent, container);
        }
        Object viewType = mAdapter.getItemViewType(position);
        View v = mRecycler.get(viewType);
        if (v == null) {
            v = mAdapter.newView(parent, viewType);
        }
        HolderImpl holder = mHolders.get(v);
        if (holder == null) {
            holder = new HolderImpl();
            mHolders.put(v, holder);
        }
        holder.position = position;
        mAdapter.bindView(container, v, holder);
        mViewTypes.put(v, viewType);
        parent.addView(v);
        return v;
    }

    @Override
    public final void destroyItem(ViewGroup container, int position, Object object) {
        View v = (View) object;
        Object itemViewType = mViewTypes.get(v);
        container.removeView(v);
        mRecycler.put(itemViewType, v);
    }

    @Override
    public final boolean isViewFromObject(View view, Object o) {
        return view == o;
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

    private static final class Recycler {

        @NonNull
        private final Map<Object, Deque<View>> mViews = new HashMap<>();

        Recycler() {
        }

        void put(@NonNull Object itemViewType, @NonNull View v) {
            Deque<View> views = mViews.get(itemViewType);
            if (views == null) {
                views = new ArrayDeque<>();
                mViews.put(itemViewType, views);
            }
            views.offer(v);
        }

        @Nullable
        View get(@NonNull Object itemViewType) {
            Deque<View> views = mViews.get(itemViewType);
            if (views == null) {
                return null;
            }
            return views.poll();
        }
    }

    private final class ViewPagerContainer extends Container {

        @NonNull
        private final ViewPager mViewPager;

        ViewPagerContainer(@NonNull ViewPager viewPager) {
            mViewPager = viewPager;
        }

        @Override
        public void scrollToPosition(int position) {
            mViewPager.setCurrentItem(position, true);
        }

        @Override
        public int getItemCount() {
            return mAdapter.getItemCount();
        }

        @NonNull
        @Override
        public ViewGroup getViewGroup() {
            return mViewPager;
        }

        @NonNull
        @Override
        public Container getRootContainer() {
            return this;
        }
    }
}
