package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewType;
import lombok.NonNull;

import java.util.WeakHashMap;

final class BindingEngine {

    @NonNull
    private final WeakHashMap<ViewType, Binder<?, ?>> mBinders = new WeakHashMap<>();

    @NonNull
    private final Mapper mMapper;

    @NonNull
    private final ItemAccessor mItemAccessor;

    BindingEngine(@NonNull Mapper mapper, @NonNull ItemAccessor itemAccessor) {
        mMapper = mapper;
        mItemAccessor = itemAccessor;
    }

    @NonNull
    private Object getItem(int position) {
        return mItemAccessor.get(position);
    }

    @NonNull
    View newView(@NonNull ViewGroup parent, @NonNull ViewType viewType) {
        Binder<?, ?> binder = mBinders.get(viewType);
        if (binder == null) {
            // Should never happen, as callers are expected to invoke getItemViewType(int) before invoking this method.
            throw new AssertionError("No binder associated with view type");
        }
        return binder.newView(parent);
    }

    void bindView(@NonNull View view, @NonNull Holder holder) {
        int position = holder.getPosition();
        Object item = getItem(position);
        binderOrThrow(item, position).bindView(item, view, holder);
    }

    @NonNull
    ViewType getItemViewType(int position) {
        Object item = getItem(position);
        Binder<Object, ?> binder = binderOrThrow(item, position);
        ViewType viewType = binder.getViewType(item, position);
        mBinders.put(viewType, binder);
        return viewType;
    }

    boolean isEnabled(int position) {
        Object item = getItem(position);
        return binderOrThrow(item, position).isEnabled(item, position);
    }

    long getItemId(int position) {
        Object item = getItem(position);
        return binderOrThrow(item, position).getItemId(item, position);
    }

    boolean hasStableIds() {
        return mMapper.hasStableIds();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private Binder<Object, View> binderOrThrow(@NonNull Object item, int position) {
        Binder<Object, View> binder = (Binder<Object, View>) mMapper.getBinder(item, position);
        if (binder == null) {
            throw new AssertionError("No binder for item " + item + " at position " + position);
        }
        return binder;
    }
}
