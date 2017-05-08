package com.nextfaze.poweradapters.data;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.util.concurrent.ExecutorService;

public final class DataExecutors {

    private static final ExecutorService DEFAULT = (ExecutorService) AsyncTask.THREAD_POOL_EXECUTOR;

    private DataExecutors() {
        throw new AssertionError();
    }

    /** Returns the default {@link ExecutorService} used by some {@link Data} instances. */
    @NonNull
    public static ExecutorService defaultExecutor() {
        return DEFAULT;
    }
}
