package com.nextfaze.poweradapters.support;

import android.database.DataSetObserver;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.view.FixedPagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.DataObserver;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.SimpleDataObserver;
import com.nextfaze.poweradapters.ViewType;
import lombok.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class ConverterPagerAdapter extends FixedPagerAdapter {

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
    private final WeakHashMap<View, ViewType> mViewTypes = new WeakHashMap<>();

    @NonNull
    private final WeakHashMap<View, HolderImpl> mHolders = new WeakHashMap<>();

    /** Keep track of observers registered, so we know when to register our own. */
    @NonNull
    private final Set<DataSetObserver> mDataSetObservers = new HashSet<>();

    @NonNull
    public final PowerAdapter mAdapter;

    public ConverterPagerAdapter(@NonNull PowerAdapter adapter) {
        mAdapter = adapter;
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
    public final Object instantiateItem(ViewGroup container, int position) {
        ViewType viewType = mAdapter.getItemViewType(position);
        View v = mRecycler.get(viewType);
        if (v == null) {
            v = mAdapter.newView(container, viewType);
        }
        HolderImpl holder = mHolders.get(v);
        if (holder == null) {
            holder = new HolderImpl();
            mHolders.put(v, holder);
        }
        holder.position = position;
        mAdapter.bindView(v, holder);
        mViewTypes.put(v, viewType);
        container.addView(v);
        return v;
    }

    @Override
    public final void destroyItem(ViewGroup container, int position, Object object) {
        View v = (View) object;
        ViewType itemViewType = mViewTypes.get(v);
        container.removeView(v);
        mRecycler.put(itemViewType, v);
    }

    @Override
    public final boolean isViewFromObject(View view, Object o) {
        return view == o;
    }

    private static final class HolderImpl implements Holder {

        int position;

        @Override
        public int getPosition() {
            return position;
        }
    }

    private static final class Recycler {

        @NonNull
        private final Map<ViewType, Deque<View>> mViews = new HashMap<>();

        void put(@NonNull ViewType itemViewType, @NonNull View v) {
            Deque<View> views = mViews.get(itemViewType);
            if (views == null) {
                views = new ArrayDeque<>();
                mViews.put(itemViewType, views);
            }
            views.offer(v);
        }

        @Nullable
        View get(@NonNull ViewType itemViewType) {
            Deque<View> views = mViews.get(itemViewType);
            if (views == null) {
                return null;
            }
            return views.poll();
        }
    }
}
