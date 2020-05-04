package com.nextfaze.poweradapters.data;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;

import static java.lang.String.format;

final class NamedThreadFactory implements ThreadFactory {

    @NonNull
    private final AtomicInteger mCount = new AtomicInteger();

    @NonNull
    private final String mNameFormat;

    NamedThreadFactory(@NonNull String nameFormat) {
        mNameFormat = nameFormat;
    }

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setName(format(mNameFormat, mCount.incrementAndGet()));
        return thread;
    }
}
