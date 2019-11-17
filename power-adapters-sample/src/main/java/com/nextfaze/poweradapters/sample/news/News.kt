package com.nextfaze.poweradapters.sample.news

import android.arch.lifecycle.ViewModel
import android.content.Context
import android.support.v7.app.AlertDialog
import androidx.annotation.ColorInt
import com.nextfaze.poweradapters.Container
import com.nextfaze.poweradapters.Holder
import com.nextfaze.poweradapters.adapterOf
import com.nextfaze.poweradapters.binding.BinderWrapper
import com.nextfaze.poweradapters.buildAdapter
import com.nextfaze.poweradapters.data.DataConditions.isEmpty
import com.nextfaze.poweradapters.data.toAdapter
import com.nextfaze.poweradapters.sample.R
import com.nextfaze.poweradapters.sample.emptyMessage
import com.nextfaze.poweradapters.sample.loadNextButton
import com.nextfaze.poweradapters.sample.loadingIndicatorWhileNonEmpty

class NewsViewModel : ViewModel() {

    val data = NewsData(15, 5)

    override fun onCleared() = data.close()
}

fun createNewsAdapter(viewModel: NewsViewModel) = buildAdapter {
    val binder = NewsItemBinder(viewModel.data.asList())

    // Header
    layoutResource(R.layout.news_header_item)

    // News items
    +viewModel.data.toAdapter(binder)

    // Loading indicator
    +loadingIndicatorWhileNonEmpty(viewModel.data)

    // Load next button
    +loadNextButton(viewModel.data)

    // Footer
    +adapterOf(R.layout.news_footer_item).showOnlyWhile(!isEmpty(viewModel.data))

    // Empty message
    +emptyMessage(viewModel.data)
}

private fun NewsItemBinder.withColor(@ColorInt color: Int) = object : BinderWrapper<NewsItem, NewsItemView>(this) {
    override fun bindView(
            container: Container,
            t: NewsItem,
            v: NewsItemView,
            holder: Holder,
            payloads: List<Any>
    ) {
        super.bindView(container, t, v, holder, payloads)
        v.setBackgroundColor(color)
    }
}

fun showNewsItemEditDialog(context: Context, newsItems: MutableList<in NewsItem>, position: Int) {
    data class Item(val title: CharSequence, val onClick: () -> Unit)
    val items = listOf(
            Item("Change", { newsItems[position] = NewsItem.create("Changed") }),
            Item("Clear", { newsItems.clear() })
    )
    AlertDialog.Builder(context)
            .setItems(items.map { it.title }.toTypedArray()) { _, which -> items[which].onClick() }
            .show()
}
