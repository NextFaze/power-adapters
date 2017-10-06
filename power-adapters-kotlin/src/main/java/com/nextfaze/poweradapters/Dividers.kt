package com.nextfaze.poweradapters

import android.support.annotation.LayoutRes
import com.nextfaze.poweradapters.DividerAdapterBuilder.EmptyPolicy
import com.nextfaze.poweradapters.DividerAdapterBuilder.EmptyPolicy.DEFAULT

/**
 * Adds dividers to the adapter.
 * @param emptyPolicy Set the policy that determines what dividers are shown if the wrapped adapter is empty. Defaults
 * to [DEFAULT].
 * @param leadingView Sets the divider that appears before the wrapped adapters items.
 * @param trailingView Sets the divider that appears after the wrapped adapters items.
 * @param innerView Sets the divider that appears between all of the wrapped adapters items.
 * @see DividerAdapterBuilder
 */
fun PowerAdapter.addDividers(emptyPolicy: EmptyPolicy = DEFAULT,
                             leadingView: ViewFactory? = null,
                             trailingView: ViewFactory? = null,
                             innerView: ViewFactory? = null) = compose {
    DividerAdapterBuilder().apply {
        emptyPolicy(emptyPolicy)
        leadingView?.let { leadingView(it) }
        trailingView?.let { trailingView(it) }
        innerView?.let { innerView(it) }
    }.build(this)
}

/**
 * Adds dividers to the adapter.
 * @param emptyPolicy Set the policy that determines what dividers are shown if the wrapped adapter is empty. Defaults
 * to [DEFAULT].
 * @param leadingResource Sets the divider that appears before the wrapped adapters items.
 * @param trailingResource Sets the divider that appears after the wrapped adapters items.
 * @param innerResource Sets the divider that appears between all of the wrapped adapters items.
 * @see DividerAdapterBuilder
 */
fun PowerAdapter.addDividers(emptyPolicy: EmptyPolicy = DEFAULT,
                             @LayoutRes leadingResource: Int = 0,
                             @LayoutRes trailingResource: Int = 0,
                             @LayoutRes innerResource: Int = 0) = compose {
    DividerAdapterBuilder().apply {
        emptyPolicy(emptyPolicy)
        if (leadingResource > 0) leadingResource(leadingResource)
        if (trailingResource > 0) trailingResource(trailingResource)
        if (innerResource > 0) innerResource(innerResource)
    }.build(this)
}