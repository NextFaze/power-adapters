package com.nextfaze.poweradapters

fun ViewFactory.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

fun Iterable<ViewFactory>.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)

fun Collection<ViewFactory>.toAdapter(): PowerAdapter = PowerAdapter.asAdapter(this)