package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.widget.TextView
import com.nextfaze.poweradapters.ViewFactories.asViewFactory

/** Creates a composite [PowerAdapter] using a type-safe builder interface. */
fun adapter(init: AdapterBuilder.() -> Unit): PowerAdapter = AdapterBuilder().apply { init() }.build()

/** Used to build a composite [PowerAdapter]. */
class AdapterBuilder internal constructor() {

    private val b = ConcatAdapterBuilder()

    internal fun build() = b.build()

    /* Layout resources */

    fun layoutResource(@LayoutRes layoutResource: Int, @StringRes textStringResource: Int? = null) =
            view(asViewFactory(layoutResource), textStringResource)

    fun layoutResource(@LayoutRes layoutResource: Int, text: CharSequence? = null) =
            view(asViewFactory(layoutResource), text)

    fun layoutResources(layoutResources: Iterable<Int>) = layoutResources.forEach { +it }

    fun layoutResources(@LayoutRes vararg layoutResources: Int) = +layoutResources.toList()

    @JvmName("addLayoutResources") operator fun Iterable<Int>.unaryPlus() = forEach { +it }

    /* Views */

    fun view(view: ViewFactory, text: CharSequence? = null) {
        b.add(view.apply { text?.let { applyToTextView { this.text = it } } })
    }

    fun view(view: ViewFactory, @StringRes textStringResource: Int? = null) {
        b.add(view.apply { textStringResource?.let { applyToTextView { setText(it) } } })
    }

    fun views(views: Iterable<ViewFactory>) = views.forEach { view(it, text = null) }

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

private fun ViewFactory.applyToTextView(body: TextView.() -> Unit) = ViewFactory { parent ->
    create(parent).apply { if (this is TextView) body() }
}