package com.nextfaze.powerdata.sample;

import lombok.NonNull;
import lombok.experimental.Accessors;

@Accessors(prefix = "m")
public final class NewsItem {

    @NonNull
    private final String mTitle;

    public NewsItem(@NonNull String title) {
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
