package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import android.view.View
import com.nextfaze.poweradapters.binding.Binder
import com.nextfaze.poweradapters.binding.Mappers.singletonMapper
import com.nextfaze.poweradapters.binding.ViewHolder
import com.nextfaze.poweradapters.binding.ViewHolderBinder

fun <T, V : View> binder(@LayoutRes layoutResource: Int,
                         bind: V.(Container, T, Holder) -> Unit): Binder<T, V> =
        Binder.create(layoutResource) { container, item, view, holder ->
            view.bind(container, item, holder)
        }

fun <T, H : ViewHolder> binder(@LayoutRes layoutResource: Int,
                               createViewHolder: (View) -> H,
                               bindViewHolder: H.(Container, T, Holder) -> Unit): Binder<T, View> =
        ViewHolderBinder.create(layoutResource, createViewHolder) { container, item, h, holder ->
            h.bindViewHolder(container, item, holder)
        }

fun Binder<*, *>.toMapper() = singletonMapper(this)