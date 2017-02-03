package com.nextfaze.poweradapters.sample;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class BlogPost extends NewsItem {

    @NonNull
    public static BlogPost create() {
        return new BlogPost("Inserted", randomType());
    }

    @NonNull
    public static BlogPost create(@NonNull String title) {
        return new BlogPost(title, randomType());
    }

    @NonNull
    public static List<BlogPost> create(int count) {
        ArrayList<BlogPost> blogPosts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            blogPosts.add(BlogPost.create());
        }
        return blogPosts;
    }

    public BlogPost(@NonNull String title, @NonNull Type type) {
        super(title, type);
    }
}
