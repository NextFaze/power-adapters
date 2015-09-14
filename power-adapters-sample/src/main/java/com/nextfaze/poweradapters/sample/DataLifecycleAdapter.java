package com.nextfaze.poweradapters.sample;

import android.os.Handler;
import com.nextfaze.asyncdata.Data;
import com.nextfaze.poweradapters.PowerAdapter;
import com.nextfaze.poweradapters.PowerAdapterWrapper;
import lombok.NonNull;

import static android.os.Looper.getMainLooper;

public final class DataLifecycleAdapter extends PowerAdapterWrapper {

    @NonNull
    private final Handler mHandler = new Handler(getMainLooper());

    @NonNull
    private final Data<?> mData;

    public DataLifecycleAdapter(@NonNull PowerAdapter adapter, @NonNull Data<?> data) {
        super(adapter);
        mData = data;
    }

    @Override
    protected void onFirstObserverRegistered() {
        super.onFirstObserverRegistered();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mData.notifyShown();
            }
        });
    }

    @Override
    protected void onLastObserverUnregistered() {
        super.onLastObserverUnregistered();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mData.notifyHidden();
            }
        });
    }
}
