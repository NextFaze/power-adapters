package com.nextfaze.poweradapters.sample

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nextfaze.poweradapters.Container
import com.nextfaze.poweradapters.Holder
import com.nextfaze.poweradapters.PowerAdapter
import com.nextfaze.poweradapters.TreeAdapter
import com.nextfaze.poweradapters.adapterOf
import com.nextfaze.poweradapters.and
import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.data.DataConditions.data
import com.nextfaze.poweradapters.data.DataConditions.isEmpty
import com.nextfaze.poweradapters.data.DataConditions.isLoading
import com.nextfaze.poweradapters.data.IncrementalArrayData
import com.nextfaze.poweradapters.rxjava2.showOnlyWhile
import io.reactivex.Observable

fun loadingIndicator(content: Observable<*>) =
        adapterOf(R.layout.list_loading_item).showOnlyWhile(content.map { false }.startWith(true))

fun loadingIndicatorWhileNonEmpty(data: Data<*>) =
        adapterOf(R.layout.list_loading_item).showOnlyWhile(data(data) { it.isLoading && !it.isEmpty })

fun loadingIndicator(data: Data<*>) =
        adapterOf(R.layout.list_loading_item).showOnlyWhile(data(data) { it.isLoading })

fun emptyMessage(content: Observable<out Collection<*>>) =
        adapterOf(R.layout.list_empty_item).showOnlyWhile(content.map { it.isEmpty() }.startWith(false))

fun emptyMessage(data: Data<*>) =
        adapterOf(R.layout.list_empty_item).showOnlyWhile(isEmpty(data) and !isLoading(data))

fun loadNextButton(data: Data<*>) = loadNextButton(data, { (data as? IncrementalArrayData<*>)?.loadNext() })

fun loadNextButton(data: Data<*>, onClick: () -> Unit): PowerAdapter {
    // TODO: This is too hacky. Would ideally be able to access Container from within a ViewFactory.
    val adapter = object : PowerAdapter() {
        override fun getItemCount(): Int {
            return 1
        }

        override fun newView(parent: ViewGroup, viewType: Any) =
                parent.context.layoutInflater.inflate(R.layout.list_load_next_item, parent, false)

        override fun bindView(container: Container, view: View, holder: Holder) {
            view.setOnClickListener {
                onClick()
                container.scrollToPosition(holder.position)
            }
        }
    }
    val dataHasMoreAvailable = data(data) { !it.isLoading && !it.isEmpty && it.available() > 0 }
    return adapter.showOnlyWhile(dataHasMoreAvailable)
}

fun PowerAdapter.nest(getChildAdapter: (Int) -> PowerAdapter): PowerAdapter {
    val treeAdapter = TreeAdapter(this, getChildAdapter)
    treeAdapter.isAutoExpand = true
    return treeAdapter
}

fun <T> MutableSet<T>.toggle(element: T) {
    if (element in this) remove(element)
    else add(element)
}

val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)

fun Context.getDrawableForAttribute(@AttrRes attr: Int): Drawable = getResourceFromAttribute(attr) {
    ContextCompat.getDrawable(this, it)
}

private fun <T> Context.getResourceFromAttribute(@AttrRes attr: Int, getResource: (Int) -> T): T {
    TypedValue().let {
        theme.resolveAttribute(attr, it, true)
        return getResource(it.resourceId)
    }
}

