package com.nextfaze.poweradapters.sample;

import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public final class NewsItem {

    @NonNull
    private final String mTitle;

    @NonNull
    public static List<NewsItem> create(int count) {
        ArrayList<NewsItem> newsItems = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newsItems.add(new NewsItem());
        }
        return newsItems;
    }

    public NewsItem() {
        mTitle = "Inserted";
    }

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
