package com.nextfaze.powerdata;

import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public abstract class SimpleDataAdapter<T> extends DataAdapter<T> {

    @LayoutRes
    private final int mItemLayoutResource;

    public SimpleDataAdapter(@NonNull Data<T> data, @LayoutRes int itemLayoutResource) {
        super(data);
        mItemLayoutResource = itemLayoutResource;
    }

    @LayoutRes
    public final int getItemLayoutResource() {
        return mItemLayoutResource;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(getLayoutInflater(parent), parent);
        }
        bindView(getItem(position), convertView, position);
        return convertView;
    }

    @NonNull
    protected View newView(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup parent) {
        return layoutInflater.inflate(mItemLayoutResource, parent, false);
    }

    protected abstract void bindView(@NonNull T t, @NonNull View v, int position);

    @NonNull
    private static LayoutInflater getLayoutInflater(@NonNull View v) {
        return LayoutInflater.from(v.getContext());
    }
}
