package com.nextfaze.poweradapters.binding;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ListAdapter;

import java.util.Collections;
import java.util.List;

import static com.nextfaze.poweradapters.binding.Mappers.singletonMapper;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/**
 * A mutable binding adapter that automatically generates notifications when its contents changes.
 * This class does not accept {@code null} elements.
 */
public final class ListBindingAdapter<E> extends ListAdapter<E> {

    @NonNull
    private final BindingEngine<E> mEngine;

    public ListBindingAdapter(@NonNull Binder<? super E, ?> binder) {
        this(singletonMapper(checkNotNull(binder, "binder")));
    }

    @SuppressWarnings("WeakerAccess")
    public ListBindingAdapter(@NonNull Mapper<? super E> mapper) {
        this(mapper, Collections.<E>emptyList());
    }

    public ListBindingAdapter(@NonNull Binder<? super E, ?> binder, @NonNull List<? extends E> list) {
        this(singletonMapper(binder), list);
    }

    public ListBindingAdapter(@NonNull Mapper<? super E> mapper, @NonNull List<? extends E> list) {
        super(list);
        mEngine = new BindingEngine<>(checkNotNull(mapper, "mapper"), new ItemAccessor<E>() {
            @NonNull
            @Override
            public E get(int position) {
                return ListBindingAdapter.this.get(position);
            }
        });
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        return mEngine.newView(parent, viewType);
    }

    @Override
    public final void bindView(
            @NonNull Container container,
            @NonNull View view,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        mEngine.bindView(container, view, holder, payloads);
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
