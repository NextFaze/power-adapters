package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import lombok.NonNull;

import java.util.WeakHashMap;

import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;

public abstract class ViewHolderBinder<T, H extends ViewHolder> implements Binder {

    @NonNull
    private final WeakHashMap<View, H> mViewHolders = new WeakHashMap<>();

    @NonNull
    private final ViewFactory mViewFactory;

    private final boolean mEnabled;

    public ViewHolderBinder(@LayoutRes int itemLayoutResource) {
        this(itemLayoutResource, true);
    }

    public ViewHolderBinder(@LayoutRes int itemLayoutResource, boolean enabled) {
        this(viewFactoryForResource(itemLayoutResource), enabled);
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
        H h = newViewHolder(mViewFactory.create(parent));
        mViewHolders.put(h.view, h);
        return h.view;
    }

    @Override
    public final void bindView(@NonNull Object obj, @NonNull View v, @NonNull Holder holder) {
        H h = mViewHolders.get(v);
        // Infrastructure ensures only the correct type is passed here.
        //noinspection unchecked
        bindViewHolder((T) obj, h, holder);
    }

    @Override
    public boolean isEnabled(int position) {
        return mEnabled;
    }

    @NonNull
    protected abstract H newViewHolder(@NonNull View v);

    protected abstract void bindViewHolder(@NonNull T t, @NonNull H h, @NonNull Holder holder);
}
