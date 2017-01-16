package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

final class BindingEngine<T> {

    @NonNull
    private final WeakMap<Object, Binder<?, ?>> mViewTypeToBinder = new WeakMap<>();

    @NonNull
    private final Mapper<? super T> mMapper;

    @NonNull
    private final ItemAccessor<? extends T> mItemAccessor;

    BindingEngine(@NonNull Mapper<? super T> mapper, @NonNull ItemAccessor<? extends T> itemAccessor) {
        mMapper = mapper;
        mItemAccessor = itemAccessor;
    }

    @NonNull
    private T getItem(int position) {
        return mItemAccessor.get(position);
    }

    @NonNull
    View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        Binder<?, ?> binder = mViewTypeToBinder.get(viewType);
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
        mViewTypeToBinder.put(viewType, binder);
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

    @NonNull
    private Binder<? super T, View> binderOrThrow(@NonNull T item, int position) {
        Binder<? super T, View> binder = mMapper.getBinder(item, position);
        if (binder == null) {
            throw new AssertionError("No binder for item " + item + " at position " + position);
        }
        return binder;
    }
}
