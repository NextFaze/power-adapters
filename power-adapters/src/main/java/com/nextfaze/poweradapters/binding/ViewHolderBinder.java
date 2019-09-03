package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.List;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

@SuppressWarnings("deprecation")
public abstract class ViewHolderBinder<T, H extends ViewHolder> extends Binder<T, View> {

    @NonNull
    private final WeakMap<View, H> mViewHolders = new WeakMap<>();

    @NonNull
    private final ViewFactory mViewFactory;

    @NonNull
    public static <T, H extends ViewHolder> ViewHolderBinder<T, H> create(@LayoutRes int layoutResource,
                                                                          @NonNull final ViewHolderFactory<H> viewHolderFactory,
                                                                          @NonNull final BindViewHolderFunction<T, H> bindFunction) {
        return create(asViewFactory(layoutResource), viewHolderFactory, bindFunction);
    }

    @NonNull
    public static <T, H extends ViewHolder> ViewHolderBinder<T, H> create(@NonNull ViewFactory viewFactory,
                                                                          @NonNull final ViewHolderFactory<H> viewHolderFactory,
                                                                          @NonNull final BindViewHolderFunction<T, H> bindFunction) {
        checkNotNull(viewHolderFactory, "viewHolderFactory");
        checkNotNull(bindFunction, "bindFunction");
        return new ViewHolderBinder<T, H>(checkNotNull(viewFactory, "viewFactory")) {
            @NonNull
            @Override
            protected H newViewHolder(@NonNull View v) {
                return viewHolderFactory.create(v);
            }

            @Override
            protected void bindViewHolder(
                    @NonNull Container container,
                    @NonNull T t,
                    @NonNull H h,
                    @NonNull Holder holder,
                    @NonNull List<Object> payloads
            ) {
                bindFunction.bindViewHolder(container, t, h, holder);
            }
        };
    }

    public ViewHolderBinder(@LayoutRes int itemLayoutResource) {
        this(asViewFactory(itemLayoutResource));
    }

    public ViewHolderBinder(@NonNull ViewFactory viewFactory) {
        mViewFactory = checkNotNull(viewFactory, "viewFactory");
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }

    @Override
    public final void bindView(
            @NonNull Container container,
            @NonNull T t,
            @NonNull View v,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        H h = mViewHolders.get(v);
        if (h == null) {
            h = newViewHolder(v);
            mViewHolders.put(v, h);
        }
        bindViewHolder(container, t, h, holder, payloads);
    }

    @NonNull
    protected abstract H newViewHolder(@NonNull View v);

    protected void bindViewHolder(
            @NonNull Container container,
            @NonNull T t,
            @NonNull H h,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        bindViewHolder(t, h, holder);
    }

    /** Use {@link #bindViewHolder(Container, T, ViewHolder, Holder, List)} instead. */
    @Deprecated
    protected void bindViewHolder(@NonNull T t, @NonNull H h, @NonNull Holder holder) {
    }
}
