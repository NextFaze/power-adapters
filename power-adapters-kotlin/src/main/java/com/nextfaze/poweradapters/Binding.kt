package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import android.view.View
import com.nextfaze.poweradapters.binding.Binder
import com.nextfaze.poweradapters.binding.BinderWrapper
import com.nextfaze.poweradapters.binding.Mapper
import com.nextfaze.poweradapters.binding.Mappers.singletonMapper
import com.nextfaze.poweradapters.binding.ViewHolder
import com.nextfaze.poweradapters.binding.ViewHolderBinder

/**
 * Creates a `Binder` that inflates its views from a layout resource.
 * @param layoutResource The layout resource to inflate.
 * @param bind A function that will be invoked to bind the object [T] to the `View` of type [V].
 * @param T The type of object this binder is capable of binding to [V] `View` instances.
 * @param V The type of `View` to which [T] instances will be bound.
 * @return A [Binder] capable of binding [T] instances to [V] `View` instances.
 */
fun <T, V : View> binder(
        @LayoutRes layoutResource: Int,
        bind: V.(Container, T, Holder) -> Unit
): Binder<T, V> = Binder.create(layoutResource) { container, item, view, holder -> view.bind(container, item, holder) }

/**
 * Creates a [ViewHolder]-based `Binder` that inflates its views from a layout resource.
 * @param layoutResource The layout resource to inflate.
 * @param createViewHolder A factory function that will be invoked to create the view holders of type [H].
 * @param bindViewHolder A function that will be invoked to bind the object [T] to the view holder of type [H].
 * @param T The type of object this binder is capable of binding.
 * @param H The type of view holder to which [T] instances will be bound.
 * @return A [Binder] capable of binding [T] instances to `View` instances.
 */
fun <T, H : ViewHolder> binder(
        @LayoutRes layoutResource: Int,
        createViewHolder: (View) -> H,
        bindViewHolder: H.(Container, T, Holder) -> Unit
): Binder<T, View> = ViewHolderBinder.create(layoutResource, createViewHolder) { container, item, h, holder ->
    h.bindViewHolder(container, item, holder)
}

/** Returns this binder as a singleton [Mapper] that only maps using this binder. */
fun Binder<*, *>.toMapper() = singletonMapper(this)

/** Returns a copy of this binder that uses the specified view type. */
fun <T, V : View> Binder<T, V>.withViewType(viewType: Any) = withViewType { _, _ -> viewType }

/** Returns a copy of this binder that uses the view type returned from the supplied function on a per-item basis. */
fun <T, V : View> Binder<T, V>.withViewType(getViewType: (T, Int) -> Any) = object : BinderWrapper<T, V>(this) {
    override fun getViewType(t: T, position: Int) = getViewType(t, position)
}

/** Returns a copy of this binder that has a [Binder.hasStableIds] value of [hasStableIds]. */
fun <T, V : View> Binder<T, V>.withStableIds(hasStableIds: Boolean) = object : BinderWrapper<T, V>(this) {
    override fun hasStableIds() = hasStableIds
}
