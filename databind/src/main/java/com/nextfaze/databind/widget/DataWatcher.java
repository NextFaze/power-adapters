package com.nextfaze.databind.widget;

import com.nextfaze.databind.Data;
import com.nextfaze.databind.DataObserver;
import com.nextfaze.databind.ErrorObserver;
import com.nextfaze.databind.LoadingObserver;
import lombok.NonNull;
import lombok.experimental.Accessors;

import javax.annotation.Nullable;

@Accessors(prefix = "m")
class DataWatcher implements DataObserver, LoadingObserver, ErrorObserver {

    @Nullable
    private Data<?> mData;

    @Nullable
    private Data<?> mDataRegistered;

    private boolean mShown;

    void setData(@Nullable Data<?> data) {
        if (data != mData) {
            mData = data;
            updateRegistration();
        }
    }

    void setShown(boolean shown) {
        if (shown != mShown) {
            mShown = shown;
            updateRegistration();
        }
    }

    private void updateRegistration() {
        Data<?> dataToRegister = mShown ? mData : null;
        if (dataToRegister != mDataRegistered) {
            if (mDataRegistered != null) {
                mDataRegistered.unregisterDataObserver(this);
                mDataRegistered.unregisterLoadingObserver(this);
                mDataRegistered.unregisterErrorObserver(this);
            }
            mDataRegistered = dataToRegister;
            if (mDataRegistered != null) {
                mDataRegistered.registerDataObserver(this);
                mDataRegistered.registerLoadingObserver(this);
                mDataRegistered.registerErrorObserver(this);
            }
        }
    }

    @Nullable
    Data<?> getData() {
        return mData;
    }

    @Override
    public void onChange() {
    }

    @Override
    public void onInvalidated() {
    }

    @Override
    public void onError(@NonNull Throwable e) {
    }

    @Override
    public void onLoadingChange() {
    }
}
