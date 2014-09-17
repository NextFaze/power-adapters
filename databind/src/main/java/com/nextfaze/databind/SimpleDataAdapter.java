package com.nextfaze.databind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public abstract class SimpleDataAdapter<T> extends BaseAdapter {

    @Getter
    @NonNull
    private final Data<T> mData;

    @Getter
    private final int mItemLayoutResource;

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

    public SimpleDataAdapter(@NonNull Data<T> data, int itemLayoutResource) {
        mData = data;
        mItemLayoutResource = itemLayoutResource;
        mData.registerDataObserver(mDataObserver);
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(parent);
        }
        bindView(getItem(position), convertView, position);
        return convertView;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    protected View newView(@NonNull ViewGroup parent) {
        return getLayoutInflater(parent).inflate(mItemLayoutResource, parent, false);
    }

    protected abstract void bindView(@NonNull T t, @NonNull View v, int position);

    @NonNull
    private LayoutInflater getLayoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}
