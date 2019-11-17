package com.nextfaze.poweradapters

import android.view.View
import androidx.annotation.CheckResult
import androidx.annotation.LayoutRes
import com.nextfaze.poweradapters.internal.AdapterUtils

@CheckResult fun ViewFactory.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

@CheckResult fun Iterable<ViewFactory>.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

@CheckResult fun Collection<ViewFactory>.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

@Suppress("UNCHECKED_CAST")
@CheckResult
fun <T : View> viewFactory(@LayoutRes layoutResource: Int, body: T.() -> Unit = {}) = ViewFactory {
    (AdapterUtils.layoutInflater(it).inflate(layoutResource, it, false) as T).apply(body)
}
