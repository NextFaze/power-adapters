package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import android.view.View
import com.nextfaze.poweradapters.internal.AdapterUtils

fun ViewFactory.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

fun Iterable<ViewFactory>.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

fun Collection<ViewFactory>.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

@Suppress("UNCHECKED_CAST")
fun <T : View> viewFactory(@LayoutRes layoutResource: Int, body: T.() -> Unit = {}) = ViewFactory {
    (AdapterUtils.layoutInflater(it).inflate(layoutResource, it, false) as T).apply(body)
}
