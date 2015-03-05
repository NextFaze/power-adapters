package com.nextfaze.databind;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;

@Accessors(prefix = "m")
public final class BindingAdapter extends ListAdapterWrapper {

    @NonNull
    private final ArrayList<Binder> mBinders;

    @NonNull
    private final IdentityHashMap<Binder, Integer> mIndexes = new IdentityHashMap<Binder, Integer>();

    @NonNull
    private final Mapper mMapper;

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter) */
    public BindingAdapter(@NonNull ListAdapter adapter,
                          @NonNull Collection<Binder> binders,
                          @NonNull Mapper mapper) {
        super(adapter);
        mBinders = new ArrayList<Binder>(binders);
        mMapper = mapper;
    }

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
    public BindingAdapter(@NonNull ListAdapter adapter,
                          @NonNull ArrayList<Binder> binders,
                          @NonNull Mapper mapper,
                          boolean takeOwnership) {
        super(adapter, takeOwnership);
        mBinders = new ArrayList<Binder>(binders);
        mMapper = mapper;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        Object item = getItem(position);
        Binder binder = mMapper.getBinder(item, position);
        if (convertView == null) {
            convertView = binder.newView(parent);
        }
        binder.bindView(item, convertView, position);
        return convertView;
    }

    @Override
    public final boolean isEnabled(int position) {
        return mMapper.getBinder(getItem(position), position).isEnabled(position);
    }

    @Override
    public final int getViewTypeCount() {
        return mBinders.size();
    }

    @Override
    public final int getItemViewType(int position) {
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
