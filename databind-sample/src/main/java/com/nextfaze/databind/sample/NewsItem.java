package com.nextfaze.databind.sample;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Accessors(prefix = "m")
public final class NewsItem {

    @NonNull
    private final String mTitle;

    public NewsItem(@NonNull String title) {
        mTitle = title;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
