package com.nextfaze.poweradapters.binding;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Holder;
import lombok.NonNull;

import java.util.WeakHashMap;

import static com.nextfaze.poweradapters.internal.AdapterUtils.layoutInflater;

public abstract class ViewHolderBinder<T, H extends ViewHolder> implements Binder {

    @NonNull
    private final WeakHashMap<View, H> mViewHolders = new WeakHashMap<>();

    @LayoutRes
    private final int mItemLayoutResource;

    private final boolean mEnabled;

    public ViewHolderBinder(@LayoutRes int itemLayoutResource) {
        this(itemLayoutResource, true);
    }

    public ViewHolderBinder(@LayoutRes int itemLayoutResource, boolean enabled) {
        mItemLayoutResource = itemLayoutResource;
        mEnabled = enabled;
    }

    @LayoutRes
    public final int getItemLayoutResource() {
        return mItemLayoutResource;
    }

    @NonNull
    @Override
    public final View newView(@NonNull ViewGroup parent) {
        H h = newViewHolder(layoutInflater(parent).inflate(mItemLayoutResource, parent, false));
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
