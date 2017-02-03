package com.nextfaze.poweradapters.sample;

import android.support.annotation.NonNull;
import com.nextfaze.poweradapters.data.ArrayData;

import java.util.ArrayList;
import java.util.List;

final class NewsMultiTypeData extends ArrayData<Object> {

    @NonNull
    private final NewsService mNewsService = new NewsService();

    @NonNull
    @Override
    protected List<Object> load() throws Exception {
        ArrayList<Object> data = new ArrayList<>();
        data.add(new NewsSection("Latest News"));
        data.addAll(mNewsService.getNews());
        return data;
    }
}
