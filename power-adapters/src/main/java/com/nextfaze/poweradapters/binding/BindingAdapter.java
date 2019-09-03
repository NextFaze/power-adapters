package com.nextfaze.poweradapters.binding;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.PowerAdapter;

import java.util.List;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

public abstract class BindingAdapter<T> extends PowerAdapter {

    @NonNull
    private final BindingEngine<T> mEngine;

    protected BindingAdapter(@NonNull Mapper<? super T> mapper) {
        mEngine = new BindingEngine<>(checkNotNull(mapper, "mapper"), new ItemAccessor<T>() {
            @NonNull
            @Override
            public T get(int position) {
                return getItem(position);
            }
        });
    }

    @NonNull
    protected abstract T getItem(int position);

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
