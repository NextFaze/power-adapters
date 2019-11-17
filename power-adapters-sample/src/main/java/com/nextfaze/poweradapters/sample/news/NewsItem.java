package com.nextfaze.poweradapters.sample.news;

import java.util.Random;

import androidx.annotation.NonNull;

public class NewsItem {

    private static final Random sRandom = new Random();

    @NonNull
    private final String mTitle;

    @NonNull
    private final Type mType;

    @NonNull
    public static NewsItem create() {
        return new NewsItem("Inserted", randomType());
    }

    @NonNull
    public static NewsItem create(@NonNull String title) {
        return new NewsItem(title, randomType());
    }

    @NonNull
    public static Type randomType() {
        Type[] types = Type.values();
        return types[sRandom.nextInt(types.length)];
    }

    public NewsItem(@NonNull String title, @NonNull Type type) {
        mTitle = title;
        mType = type;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public Type getType() {
        return mType;
    }

    @Override
    public String toString() {
        return mTitle;
    }

    public enum Type {
        ENTERTAINMENT, FINANCE, POLITICS
    }
}
