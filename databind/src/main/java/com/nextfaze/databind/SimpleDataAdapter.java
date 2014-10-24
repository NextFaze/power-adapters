package com.nextfaze.databind;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public abstract class SimpleDataAdapter<T> extends DataAdapter<T> {

    @Getter
    private final int mItemLayoutResource;

    public SimpleDataAdapter(@NonNull Data<T> data, int itemLayoutResource) {
        super(data);
        mItemLayoutResource = itemLayoutResource;
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
