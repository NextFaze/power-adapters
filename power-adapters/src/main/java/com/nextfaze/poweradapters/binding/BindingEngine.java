package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import lombok.NonNull;

import java.util.WeakHashMap;

final class BindingEngine<T> {

    @NonNull
    private final WeakHashMap<Object, Binder<? super T, ?>> mBinders = new WeakHashMap<>();

    @NonNull
    private final Mapper mMapper;

    @NonNull
    private final ItemAccessor<T> mItemAccessor;

    BindingEngine(@NonNull Mapper mapper, @NonNull ItemAccessor<T> itemAccessor) {
        mMapper = mapper;
        mItemAccessor = itemAccessor;
    }

    @NonNull
    private T getItem(int position) {
        return mItemAccessor.get(position);
    }

    @NonNull
    View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        Binder<?, ?> binder = mBinders.get(viewType);
        if (binder == null) {
            // Should never happen, as callers are expected to invoke getItemViewType(int) before invoking this method.
            throw new AssertionError("No binder associated with view type");
        }
        return binder.newView(parent);
    }

    void bindView(@NonNull Container container, @NonNull View view, @NonNull Holder holder) {
        int position = holder.getPosition();
        T item = getItem(position);
        binderOrThrow(item, position).bindView(container, item, view, holder);
    }

    @NonNull
    Object getItemViewType(int position) {
        T item = getItem(position);
        Binder<? super T, ?> binder = binderOrThrow(item, position);
        Object viewType = binder.getViewType(item, position);
        mBinders.put(viewType, binder);
        return viewType;
    }

    boolean isEnabled(int position) {
        T item = getItem(position);
        return binderOrThrow(item, position).isEnabled(item, position);
    }

    long getItemId(int position) {
        T item = getItem(position);
        return binderOrThrow(item, position).getItemId(item, position);
    }

    boolean hasStableIds() {
        return mMapper.hasStableIds();
    }

    @SuppressWarnings("unchecked")
    @NonNull
    private Binder<? super T, View> binderOrThrow(@NonNull Object item, int position) {
        Binder<T, View> binder = (Binder<T, View>) mMapper.getBinder(item, position);
        if (binder == null) {
            throw new AssertionError("No binder for item " + item + " at position " + position);
        }
        return binder;
    }
}
