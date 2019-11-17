package com.nextfaze.poweradapters;

import android.view.View;
import android.view.ViewGroup;

import com.nextfaze.poweradapters.internal.WeakMap;

import java.util.List;

import androidx.annotation.NonNull;

import static com.nextfaze.poweradapters.internal.Preconditions.checkNotNull;

/** Wraps an item with another {@link ViewGroup}, allowing for additional decorations or layouts to be applied. */
final class WrappingAdapter extends PowerAdapterWrapper {

    @NonNull
    private final WeakMap<Object, ViewTypeWrapper> mViewTypes = new WeakMap<>();

    @NonNull
    private final ViewFactory mWrapperViewFactory;

    WrappingAdapter(@NonNull PowerAdapter adapter, @NonNull ViewFactory wrapperViewFactory) {
        super(adapter);
        mWrapperViewFactory = checkNotNull(wrapperViewFactory, "wrapperViewFactory");
    }

    @NonNull
    @Override
    public Object getItemViewType(int position) {
        Object innerViewType = super.getItemViewType(position);
        ViewTypeWrapper viewTypeWrapper = mViewTypes.get(innerViewType);
        if (viewTypeWrapper == null) {
            viewTypeWrapper = new ViewTypeWrapper();
            mViewTypes.put(innerViewType, viewTypeWrapper);
        }
        viewTypeWrapper.viewType = innerViewType;
        return viewTypeWrapper;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent, @NonNull Object viewType) {
        ViewTypeWrapper viewTypeWrapper = (ViewTypeWrapper) viewType;
        View wrapperView = mWrapperViewFactory.create(parent);
        if (!(wrapperView instanceof ViewGroup)) {
            throw new IllegalArgumentException("Wrapper view must be a ViewGroup");
        }
        ViewGroup viewGroup = (ViewGroup) wrapperView;
        View childView = super.newView(viewGroup, viewTypeWrapper.viewType);
        viewGroup.addView(childView);
        return viewGroup;
    }

    @Override
    public void bindView(
            @NonNull Container container,
            @NonNull View view,
            @NonNull Holder holder,
            @NonNull List<Object> payloads
    ) {
        ViewGroup viewGroup = (ViewGroup) view;
        View childView = viewGroup.getChildAt(0);
        super.bindView(container, childView, holder, payloads);
    }

    private static final class ViewTypeWrapper {
        @NonNull
        Object viewType;

        ViewTypeWrapper() {
        }
    }
}
