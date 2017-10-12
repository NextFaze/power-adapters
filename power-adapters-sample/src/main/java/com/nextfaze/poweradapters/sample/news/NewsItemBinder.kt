package com.nextfaze.poweradapters.sample.news

import android.view.View
import android.view.ViewGroup
import com.nextfaze.poweradapters.Container
import com.nextfaze.poweradapters.Holder
import com.nextfaze.poweradapters.binding.Binder
import com.nextfaze.poweradapters.sample.R

import com.nextfaze.poweradapters.sample.layoutInflater
import java.lang.Math.max
import java.util.Collections.emptySet

class NewsItemBinder(private val newsItems: MutableList<in NewsItem>) : Binder<NewsItem, NewsItemView>() {

    override fun newView(parent: ViewGroup): View =
            parent.context.layoutInflater.inflate(R.layout.news_item_binder, parent, false)

    override fun bindView(
            container: Container,
            newsItem: NewsItem,
            v: NewsItemView,
            holder: Holder
    ) {
        v.newsItem = newsItem
        v.tags = emptySet()
        v.setOnClickListener { onClick(container, v, holder.position) }
        v.onRemoveListener = { onRemove(holder.position, it) }
        v.onInsertBeforeListener = { onInsertBefore(holder.position, it) }
        v.onInsertAfterListener = { onInsertAfter(holder.position, it) }
    }

    private fun onClick(container: Container, v: NewsItemView, position: Int) {
        container.scrollToPosition(position)
        showNewsItemEditDialog(v.context, newsItems, position)
    }

    private fun onRemove(position: Int, count: Int) {
        if (count == 1) {
            newsItems.removeAt(position)
        } else if (count > 1) {
            val index = max(0, position - count / 2)
            var i = 0
            while (i < count && newsItems.size > 0) {
                newsItems.removeAt(index)
                i++
            }
        }
    }

    private fun onInsertBefore(position: Int, count: Int) {
        newsItems.addAll(position, BlogPost.create(count))
    }

    private fun onInsertAfter(position: Int, count: Int) {
        newsItems.addAll(position + 1, BlogPost.create(count))
    }
}
