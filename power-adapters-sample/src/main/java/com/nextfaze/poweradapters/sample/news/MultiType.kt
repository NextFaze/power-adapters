package com.nextfaze.poweradapters.sample.news

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.view.View
import android.widget.TextView
import com.nextfaze.poweradapters.Container
import com.nextfaze.poweradapters.DividerAdapterBuilder.EmptyPolicy.SHOW_LEADING
import com.nextfaze.poweradapters.Holder
import com.nextfaze.poweradapters.adapterOf
import com.nextfaze.poweradapters.addDividers
import com.nextfaze.poweradapters.binder
import com.nextfaze.poweradapters.binding.BinderWrapper
import com.nextfaze.poweradapters.binding.MapperBuilder
import com.nextfaze.poweradapters.binding.ViewHolder
import com.nextfaze.poweradapters.buildAdapter
import com.nextfaze.poweradapters.data.DataConditions.isEmpty
import com.nextfaze.poweradapters.data.toAdapter
import com.nextfaze.poweradapters.sample.R
import com.nextfaze.poweradapters.sample.news.NewsItem.Type.POLITICS

class MultiTypeViewModel : ViewModel() {
    val data = NewsMultiTypeData()
}

@SuppressLint("SetTextI18n")
private val blogPostBinder = binder<BlogPost, BlogPostHolder>(android.R.layout.simple_list_item_1, ::BlogPostHolder) { _, blogPost, _ ->
    labelView.text = "Blog: " + blogPost.title
}

fun createMultiTypeAdapter(viewModel: MultiTypeViewModel) = buildAdapter {
    // Header
    +adapterOf(R.layout.news_header_item).showOnlyWhile(!isEmpty(viewModel.data))

    // Items
    val politicsNewsItemBinder = object : BinderWrapper<NewsItem, NewsItemView>(NewsItemBinder(viewModel.data.asList())) {
        override fun bindView(
                container: Container,
                newsItem: NewsItem,
                v: NewsItemView,
                holder: Holder,
                payloads: List<Any>
        ) {
            super.bindView(container, newsItem, v, holder, payloads)
            v.tags = setOf("Boring!")
        }
    }
    +viewModel.data.toAdapter(MapperBuilder<Any>()
            .bind(NewsSection::class.java, NewsSectionBinder())
            .bind(NewsItem::class.java, politicsNewsItemBinder) { it.type == POLITICS }
            .bind(NewsItem::class.java, NewsItemBinder(viewModel.data.asList()))
            .bind(BlogPost::class.java, blogPostBinder)
            .build())
}.addDividers(
        leadingResource = R.layout.list_divider_item,
        innerResource = R.layout.list_divider_item,
        trailingResource = R.layout.list_divider_item,
        emptyPolicy = SHOW_LEADING
)

private class BlogPostHolder(view: View) : ViewHolder(view) {
    val labelView: TextView = view.findViewById<View>(android.R.id.text1) as TextView
}
