package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import com.nextfaze.poweradapters.PowerAdapter.asAdapter
import com.nextfaze.poweradapters.ViewFactories.asViewFactory

fun adapterOf(vararg views: ViewFactory) = asAdapter(*views)
@JvmName("adapterOfViews") fun adapterOf(views: Iterable<ViewFactory>) = asAdapter(views)
fun adapterOf(@LayoutRes vararg layoutResources: Int) = asAdapter(*layoutResources)
@JvmName("adapterOfResources") fun adapterOf(layoutResources: Iterable<Int>) =
        asAdapter(layoutResources.map { asViewFactory(it) })

operator fun PowerAdapter.plus(adapter: PowerAdapter) = append(adapter)

operator fun PowerAdapter.plus(adapters: Iterable<PowerAdapter>) = adapter { adapter(this@plus); adapters(adapters) }

operator fun PowerAdapter.plus(view: ViewFactory) = append(view)

@JvmName("plusViews") operator fun PowerAdapter.plus(views: Iterable<ViewFactory>) =
        adapter { adapter(this@plus); views(views) }

operator fun PowerAdapter.plus(@LayoutRes layoutResource: Int) = append(layoutResource)

@JvmName("plusLayoutResources") operator fun PowerAdapter.plus(layoutResources: Iterable<Int>) =
        adapter { adapter(this@plus); layoutResources(layoutResources) }

operator fun PowerAdapter.plusAssign(dataObserver: DataObserver) = registerDataObserver(dataObserver)
operator fun PowerAdapter.minusAssign(dataObserver: DataObserver) = unregisterDataObserver(dataObserver)