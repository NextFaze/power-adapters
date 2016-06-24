package com.nextfaze.poweradapters.binding;

import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ListAdapter;
import lombok.NonNull;

import java.util.Collections;
import java.util.List;

import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;

/**
 * A mutable binding adapter that automatically generates notifications when its contents changes.
 * This class does not accept {@code null} elements.
 */
public final class ListBindingAdapter<E> extends ListAdapter<E> {

    @NonNull
    private final BindingEngine mEngine;

    public ListBindingAdapter(@NonNull Binder<?, ?> binder) {
        this(singletonMapper(binder));
    }

    @SuppressWarnings("WeakerAccess")
    public ListBindingAdapter(@NonNull Mapper mapper) {
        this(mapper, Collections.<E>emptyList());
    }

    public ListBindingAdapter(@NonNull Binder<?, ?> binder, @NonNull List<? extends E> list) {
        this(singletonMapper(binder), list);
    }

    public ListBindingAdapter(@NonNull Mapper mapper, @NonNull List<? extends E> list) {
        super(list);
        ItemAccessor itemAccessor = new ItemAccessor() {
            @NonNull
            @Override
            public Object get(int position) {
                return ListBindingAdapter.this.get(position);
            }
        };
        mEngine = new BindingEngine(mapper, itemAccessor);
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        return mEngine.newView(parent, viewType);
    }

    @Override
    public final void bindView(@NonNull View view, @NonNull Holder holder) {
        mEngine.bindView(view, holder);
    }

    @NonNull
    @Override
    public final Object getItemViewType(int position) {
        return mEngine.getItemViewType(position);
    }

    @Override
    public final boolean isEnabled(int position) {
        return mEngine.isEnabled(position);
    }

    @Override
    public final long getItemId(int position) {
        return mEngine.getItemId(position);
    }

    @Override
    public final boolean hasStableIds() {
        return mEngine.hasStableIds();
    }
}
