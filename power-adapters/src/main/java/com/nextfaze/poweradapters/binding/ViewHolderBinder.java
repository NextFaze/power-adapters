package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;

public abstract class ViewHolderBinder<T, H extends ViewHolder> extends AbstractBinder<T, View> {

    @NonNull
    private final WeakMap<View, H> mViewHolders = new WeakMap<>();

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

    public ViewHolderBinder(@LayoutRes int itemLayoutResource) {
        this(itemLayoutResource, true);
    }

    public ViewHolderBinder(@LayoutRes int itemLayoutResource, boolean enabled) {
        this(asViewFactory(itemLayoutResource), enabled);
    }

    public ViewHolderBinder(@NonNull ViewFactory viewFactory) {
        this(viewFactory, true);
    }

    public ViewHolderBinder(@NonNull ViewFactory viewFactory, boolean enabled) {
        mViewFactory = viewFactory;
        mEnabled = enabled;
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent) {
        return mViewFactory.create(parent);
    }

    @Override
    public final void bindView(@NonNull T t, @NonNull View v, @NonNull Holder holder) {
        H h = mViewHolders.get(v);
        if (h == null) {
            h = newViewHolder(v);
            mViewHolders.put(v, h);
        }
        //noinspection unchecked
        bindViewHolder(t, h, holder);
    }

    @Override
    public boolean isEnabled(@NonNull T t, int position) {
        return mEnabled;
    }

    @NonNull
    protected abstract H newViewHolder(@NonNull View v);

    protected abstract void bindViewHolder(@NonNull T t, @NonNull H h, @NonNull Holder holder);
}
