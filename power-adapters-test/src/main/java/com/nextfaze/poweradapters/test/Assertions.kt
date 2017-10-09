package com.nextfaze.poweradapters.test

internal inline fun verify(value: Boolean, lazyMessage: () -> Any) {
    if (!value) throw AssertionError(lazyMessage().toString())
}
