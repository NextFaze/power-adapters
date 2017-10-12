package com.nextfaze.poweradapters.sample.news

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.nextfaze.poweradapters.sample.R
import kotlinx.android.synthetic.main.news_item_view.view.*
import kotlin.properties.Delegates.observable

private const val CLICK_COUNT = 1
private const val LONG_CLICK_COUNT = 5

@SuppressLint("SetTextI18n")
class NewsItemView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    
    var newsItem by observable<NewsItem?>(null) { _, _, newsItem ->
        if (newsItem != null) titleView.text = "${newsItem.title} (${newsItem.type.toString().toLowerCase()})"
    }
    
    var tags by observable(emptySet<String>()) { _, _, tags ->
        val formatted = formatTags(tags)
        tagsView.text = formatted
        tagsView.visibility = if (formatted != null) VISIBLE else GONE
    }
    
    var onRemoveListener: ((Int) -> Unit)? = null

    var onInsertBeforeListener: ((Int) -> Unit)? = null

    var onInsertAfterListener: ((Int) -> Unit)? = null

    init {
        inflate(context, R.layout.news_item_view, this)
        removeButton.setOnClickListener { onRemoveListener?.invoke(CLICK_COUNT) }
        removeButton.setOnLongClickListener { onRemoveListener?.invoke(LONG_CLICK_COUNT); true }
        insertBeforeButton.setOnClickListener { onInsertBeforeListener?.invoke(CLICK_COUNT) }
        insertBeforeButton.setOnLongClickListener { onInsertBeforeListener?.invoke(LONG_CLICK_COUNT); true }
        insertAfterButton.setOnClickListener { onInsertAfterListener?.invoke(CLICK_COUNT) }
        insertAfterButton.setOnLongClickListener { onInsertAfterListener?.invoke(LONG_CLICK_COUNT); true }
    }
}

private fun formatTags(tags: Set<String>) = if (tags.isEmpty()) null else tags.joinToString(", ")
