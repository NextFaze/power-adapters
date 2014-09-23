package com.nextfaze.databind;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class DataAdapterBuilder {

    @NonNull
    private final Data<?> mData;

    @NonNull
    private final Map<Class<?>, Binder> mBinders = new HashMap<Class<?>, Binder>();

    public DataAdapterBuilder(@NonNull Data<?> data) {
        mData = data;
    }

    @NonNull
    public DataAdapterBuilder bind(@NonNull Class<?> itemClass, @NonNull Binder binder) {
        mBinders.put(itemClass, binder);
        return this;
    }

    @NonNull
    public ListAdapter build() {
        if (mBinders.isEmpty()) {
            throw new IllegalStateException("Must have at least one binder defined");
        }
        return new DataAdapter(mData, mBinders.values(), new Mapper() {
            @NonNull
            @Override
            public Binder getBinder(@NonNull Object item, int position) {
                Class<?> itemClass = item.getClass();
                Binder binder = mBinders.get(itemClass);
                if (binder == null) {
                    throw new IllegalStateException("No binder found for item class " + itemClass);
                }
                return binder;
            }
        });
    }

    final class DataAdapter extends BaseAdapter {

        @NonNull
        private final Data<?> mData;

        @NonNull
        private final List<Binder> mBinders;

        @NonNull
        private final IdentityHashMap<Binder, Integer> mIndexes = new IdentityHashMap<Binder, Integer>();

        @NonNull
        private final Mapper mMapper;

        @NonNull
        private final DataObserver mDataObserver = new DataObserver() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }

            @Override
            public void onInvalidated() {
                notifyDataSetInvalidated();
            }
        };

        private DataAdapter(@NonNull Data<?> data,
                            @NonNull Collection<Binder> binders,
                            @NonNull Mapper mapper) {
            mBinders = new ArrayList<Binder>(binders);
            mMapper = mapper;
            mData = data;
            mData.registerDataObserver(mDataObserver);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Object item = getItem(position);
            Binder binder = mMapper.getBinder(item, position);
            if (convertView == null) {
                convertView = binder.newView(parent);
            }
            binder.bindView(item, convertView, position);
            return convertView;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return mMapper.getBinder(getItem(position), position).isEnabled(position);
        }

        @Override
        public int getViewTypeCount() {
            return mBinders.size();
        }

        @Override
        public int getItemViewType(int position) {
            Object item = getItem(position);
            Binder binder = mMapper.getBinder(item, position);
            // Cache index of each binder to avoid linear search each time.
            Integer index = mIndexes.get(binder);
            if (index == null) {
                index = mBinders.indexOf(binder);
                mIndexes.put(binder, index);
            }
            return index;
        }
    }
}
