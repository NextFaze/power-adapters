package com.nextfaze.poweradapters.sample;

import android.app.Application;
import com.squareup.leakcanary.LeakCanary;

public final class SampleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
