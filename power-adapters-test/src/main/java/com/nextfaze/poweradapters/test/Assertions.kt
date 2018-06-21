package com.nextfaze.poweradapters.test

internal inline fun verify(value: Boolean, lazyMessage: () -> Any) {
    if (!value) throw AssertionError(lazyMessage().toString())
}

internal fun format(message: String?, expected: Any, actual: Any): String {
    var formatted = ""
    if (message != null && message.isNotEmpty()) formatted = "$message "
    return formatted + "expected:<" + expected + "> but was:<" + actual + ">"
}
