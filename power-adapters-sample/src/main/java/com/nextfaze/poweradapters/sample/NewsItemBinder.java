package com.nextfaze.poweradapters.sample;

import com.nextfaze.poweradapters.Container;
import com.nextfaze.poweradapters.Holder;
import com.nextfaze.poweradapters.binding.AbstractBinder;
import lombok.NonNull;

import java.util.List;

import static com.nextfaze.poweradapters.sample.Utils.showEditDialog;
import static java.lang.Math.max;
import static java.util.Collections.emptySet;

class NewsItemBinder extends AbstractBinder<NewsItem, NewsItemView> {

    @NonNull
    private final List<Object> mList;

    NewsItemBinder(@NonNull List<Object> list) {
        super(R.layout.news_item_binder);
        mList = list;
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
