package com.nextfaze.poweradapters.sample;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;
import lombok.NonNull;

public final class SampleApplication extends Application {

    @NonNull
    private final NewsSimpleData mData = new NewsSimpleData();

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }

    @NonNull
    NewsSimpleData getLongLivedData() {
        return mData;
    }
}
