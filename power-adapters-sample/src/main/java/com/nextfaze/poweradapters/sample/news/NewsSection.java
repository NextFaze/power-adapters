package com.nextfaze.poweradapters.sample.news;

import android.support.annotation.NonNull;

public class NewsSection {

    @NonNull
    private final String mTitle;

    public NewsSection(@NonNull String title) {
        mTitle = title;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
