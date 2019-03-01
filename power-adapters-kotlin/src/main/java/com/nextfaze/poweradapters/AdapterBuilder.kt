package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import com.nextfaze.poweradapters.ViewFactories.asViewFactory

/** Creates a composite [PowerAdapter] using a type-safe builder interface. */
fun buildAdapter(init: AdapterBuilder.() -> Unit): PowerAdapter = AdapterBuilder().apply { init() }.build()

@Deprecated("Use more idiomatic buildAdapter()", ReplaceWith("buildAdapter(init)"))
fun adapter(init: AdapterBuilder.() -> Unit): PowerAdapter = buildAdapter(init)

@DslMarker annotation class AdapterMarker

/** Used to build a composite [PowerAdapter]. */
@AdapterMarker class AdapterBuilder internal constructor() {

    private val b = ConcatAdapterBuilder()

    internal fun build() = b.build()

    /* Layout resources */

    fun layoutResource(@LayoutRes layoutResource: Int) = view(asViewFactory(layoutResource))

    fun layoutResources(layoutResources: Iterable<Int>) = layoutResources.forEach { +it }

    fun layoutResources(@LayoutRes vararg layoutResources: Int) = +layoutResources.toList()

    @JvmName("addLayoutResources") operator fun Iterable<Int>.unaryPlus() = forEach { +it }

    /* Views */

    fun view(view: ViewFactory) {
        b.add(view)
    }

    fun views(views: Iterable<ViewFactory>) = views.forEach { view(it) }

    fun views(vararg views: ViewFactory) = views(views.toList())

    @JvmName("addView") operator fun ViewFactory.unaryPlus() {
        b.add(this)
    }

    @JvmName("addViews") operator fun Iterable<ViewFactory>.unaryPlus() = forEach { +it }

    /* Adapters */

    fun adapter(adapter: PowerAdapter) {
        b.add(adapter)
    }

    fun adapters(adapters: Iterable<PowerAdapter>) = adapters.forEach { adapter(it) }

    fun adapters(vararg adapters: PowerAdapter) = adapters(adapters.toList())

    @JvmName("addAdapter") operator fun PowerAdapter.unaryPlus() {
        b.add(this)
    }

    @JvmName("addAdapters") operator fun Iterable<PowerAdapter>.unaryPlus() = forEach { +it }
}
