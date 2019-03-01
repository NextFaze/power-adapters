package com.nextfaze.poweradapters

import android.annotation.SuppressLint
import android.support.annotation.CheckResult
import android.support.annotation.LayoutRes
import com.nextfaze.poweradapters.PowerAdapter.asAdapter
import com.nextfaze.poweradapters.ViewFactories.asViewFactory
import com.nextfaze.poweradapters.binding.Binder
import com.nextfaze.poweradapters.binding.ListBindingAdapter
import com.nextfaze.poweradapters.binding.Mapper

@CheckResult fun adapterOf(vararg views: ViewFactory) = asAdapter(*views)

@JvmName("adapterOfViews")
@CheckResult fun adapterOf(views: Iterable<ViewFactory>) = asAdapter(views)

@CheckResult fun adapterOf(@LayoutRes vararg layoutResources: Int) = asAdapter(*layoutResources)

@JvmName("adapterOfResources") @SuppressLint("CheckResult")
@CheckResult fun adapterOf(layoutResources: Iterable<Int>) = asAdapter(layoutResources.map { asViewFactory(it) })

@CheckResult fun <T> List<T>.toAdapter(binder: Binder<in T, *>) = ListBindingAdapter(binder, this)

@CheckResult fun <T> List<T>.toAdapter(mapper: Mapper<in T>) = ListBindingAdapter(mapper, this)

@CheckResult operator fun PowerAdapter.plus(adapter: PowerAdapter) = append(adapter)

@CheckResult operator fun PowerAdapter.plus(adapters: Iterable<PowerAdapter>) = adapter { adapter(this@plus); adapters(adapters) }

@CheckResult operator fun PowerAdapter.plus(view: ViewFactory) = append(view)

@JvmName("plusViews")
@CheckResult
operator fun PowerAdapter.plus(views: Iterable<ViewFactory>) = adapter { adapter(this@plus); views(views) }

@CheckResult operator fun PowerAdapter.plus(@LayoutRes layoutResource: Int) = append(layoutResource)

@JvmName("plusLayoutResources")
@CheckResult
operator fun PowerAdapter.plus(layoutResources: Iterable<Int>) =
        adapter { adapter(this@plus); layoutResources(layoutResources) }

operator fun PowerAdapter.plusAssign(dataObserver: DataObserver) = registerDataObserver(dataObserver)

operator fun PowerAdapter.minusAssign(dataObserver: DataObserver) = unregisterDataObserver(dataObserver)
