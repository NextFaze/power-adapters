package com.nextfaze.poweradapters.sample;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.Binder;

import java.util.List;

import static com.nextfaze.poweradapters.ViewFactories.asViewFactory;
import static com.nextfaze.poweradapters.sample.Utils.showEditDialog;
import static java.lang.Math.max;
import static java.util.Collections.emptySet;

class NewsItemBinder extends Binder<NewsItem, NewsItemView> {

    @NonNull
    private final List<? super NewsItem> mList;

    NewsItemBinder(@NonNull List<? super NewsItem> list) {
        mList = list;
    }

    @NonNull
    @Override
    public View newView(@NonNull ViewGroup parent) {
        return asViewFactory(R.layout.news_item_binder).create(parent);
    }

    @Override
    public void bindView(@NonNull Container container,
                         @NonNull NewsItem newsItem,
                         @NonNull NewsItemView v,
                         @NonNull Holder holder) {
        v.setNewsItem(newsItem);
        v.setTags(emptySet());
        v.setOnClickListener(v1 -> onClick(container, v, holder.getPosition()));
        v.setOnRemoveListener(count -> onRemove(holder.getPosition(), count));
        v.setOnInsertBeforeListener(count -> onInsertBefore(holder.getPosition(), count));
        v.setOnInsertAfterListener(count -> onInsertAfter(holder.getPosition(), count));
    }

    private void onClick(@NonNull Container container, @NonNull NewsItemView v, int position) {
        container.scrollToPosition(position);
        showEditDialog(v.getContext(), mList, position);
    }

    private void onRemove(int position, int count) {
        if (count == 1) {
            mList.remove(position);
        } else if (count > 1) {
            int index = max(0, position - count / 2);
            for (int i = 0; i < count && mList.size() > 0; i++) {
                mList.remove(index);
            }
        }
    }

    private void onInsertBefore(int position, int count) {
        mList.addAll(position, BlogPost.create(count));
    }

    private void onInsertAfter(int position, int count) {
        mList.addAll(position + 1, BlogPost.create(count));
    }
}
