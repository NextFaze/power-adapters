package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.ArrayList;
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
    public BindingAdapter(@NonNull ListAdapter adapter, @NonNull Mapper mapper) {
        super(adapter);
        mBinders = new ArrayList<Binder>(mapper.getAllBinders());
        mMapper = mapper;
    }

    /** @see ListAdapterWrapper#ListAdapterWrapper(ListAdapter, boolean) */
    public BindingAdapter(@NonNull ListAdapter adapter, @NonNull Mapper mapper, boolean takeOwnership) {
        super(adapter, takeOwnership);
        mBinders = new ArrayList<Binder>(mapper.getAllBinders());
        mMapper = mapper;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        Object item = getItem(position);
        // Mappers can only handle non-null items.
        if (item != null) {
            Binder binder = mMapper.getBinder(item, position);
            if (binder != null) {
                // Invoke the binder to handle view creation and reuse.
                if (convertView == null) {
                    convertView = binder.newView(parent);
                }
                binder.bindView(item, convertView, position);
                return convertView;
            }
        }
        // Fall back to inner adapter if we can't bind this item ourselves.
        return super.getView(position, convertView, parent);
    }

    @Override
    public final boolean isEnabled(int position) {
        Object item = getItem(position);
        // Mappers can only handle non-null items.
        if (item != null) {
            Binder binder = mMapper.getBinder(item, position);
            if (binder != null) {
                return binder.isEnabled(position);
            }
        }
        // Fall back to inner adapter if we can't handle this item.
        return super.isEnabled(position);
    }

    @Override
    public final int getViewTypeCount() {
        return super.getViewTypeCount() + mBinders.size();
    }

    @Override
    public final int getItemViewType(int position) {
        Object item = getItem(position);
        // Mappers can only handle non-null items.
        if (item != null) {
            Binder binder = mMapper.getBinder(item, position);
            if (binder != null) {
                // Cache index of each binder to avoid linear search each time.
                Integer index = mIndexes.get(binder);
                if (index == null) {
                    index = mBinders.indexOf(binder);
                    mIndexes.put(binder, index);
                }
                // Offset type by inner type count, otherwise we could get collisions.
                return super.getViewTypeCount() + index;
            }
        }
        // Fall back to inner adapter if we can't bind this item ourselves.
        return super.getItemViewType(position);
    }
}
