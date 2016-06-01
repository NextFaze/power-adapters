package com.nextfaze.poweradapters.binding;

import android.view.View;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewType;
import com.nextfaze.poweradapters.ViewTypes;
import lombok.NonNull;

public abstract class AbstractBinder<T, V extends View> implements Binder<T, V> {

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    @Override
    public boolean isEnabled(@NonNull T t, int position) {
        return true;
    }

    @Override
    public long getItemId(@NonNull T t, int position) {
        return PowerAdapter.NO_ID;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @NonNull
    @Override
    public ViewType getViewType(@NonNull T t, int position) {
        return mViewType;
    }
}
