package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.ViewFactory;
import com.nextfaze.poweradapters.internal.WeakMap;
import lombok.NonNull;

import static com.nextfaze.poweradapters.ViewFactories.viewFactoryForResource;

public abstract class ViewHolderBinder<T, H extends ViewHolder> extends AbstractBinder {

    @NonNull
    private final WeakMap<View, H> mViewHolders = new WeakMap<>();

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
        return mViewFactory.create(parent);
    }

    @Override
    public final void bindView(@NonNull Object obj, @NonNull View v, @NonNull Holder holder) {
        H h = mViewHolders.get(v);
        if (h == null) {
            h = newViewHolder(v);
            mViewHolders.put(v, h);
        }
        //noinspection unchecked
        bindViewHolder((T) obj, h, holder);
    }

    @Override
    public final boolean isEnabled(@NonNull Object obj, int position) {
        //noinspection unchecked
        return isEnabledChecked((T) obj, position);
    }

    @Override
    public final long getItemId(@NonNull Object obj, int position) {
        //noinspection unchecked
        return getItemIdChecked((T) obj, position);
    }

    protected long getItemIdChecked(@NonNull T t, int position) {
        return super.getItemId(t, position);
    }

    @SuppressWarnings("UnusedParameters")
    protected boolean isEnabledChecked(@NonNull T t, int position) {
        return mEnabled;
    }

    @NonNull
    protected abstract H newViewHolder(@NonNull View v);

    protected abstract void bindViewHolder(@NonNull T t, @NonNull H h, @NonNull Holder holder);
}
