package com.nextfaze.poweradapters.sample;

import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.squareup.leakcanary.LeakCanary;

import io.reactivex.plugins.RxJavaPlugins;

public final class Application extends android.app.Application {

    private static final String TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        RxJavaPlugins.setErrorHandler(e -> Log.e(TAG, "RxJava error", e));
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        LeakCanary.install(this);
    }
}
