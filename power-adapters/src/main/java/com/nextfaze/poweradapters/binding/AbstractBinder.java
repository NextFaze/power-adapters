package com.nextfaze.poweradapters.binding;

import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.ViewType;
import com.nextfaze.poweradapters.ViewTypes;
import lombok.NonNull;

public abstract class AbstractBinder implements Binder {

    @NonNull
    private final ViewType mViewType = ViewTypes.create();

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return PowerAdapter.NO_ID;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @NonNull
    @Override
    public ViewType getViewType() {
        return mViewType;
    }
}
