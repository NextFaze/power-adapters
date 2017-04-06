package com.nextfaze.poweradapters.data

import com.nextfaze.poweradapters.DataObserver

fun <T> dataOf(vararg elements: T): Data<T> = ImmutableData.of(*elements)

fun <T> List<T>.toData(): Data<T> = ImmutableData.of(this)

val Data<*>.size get() = size()
val Data<*>.available get() = available()

operator fun <T> Data<T>.plusAssign(dataObserver: DataObserver) = registerDataObserver(dataObserver)
operator fun <T> Data<T>.minusAssign(dataObserver: DataObserver) = unregisterDataObserver(dataObserver)

operator fun <T> Data<T>.plusAssign(availableObserver: AvailableObserver) = registerAvailableObserver(availableObserver)
operator fun <T> Data<T>.minusAssign(availableObserver: AvailableObserver) = unregisterAvailableObserver(availableObserver)

operator fun <T> Data<T>.plusAssign(loadingObserver: LoadingObserver) = registerLoadingObserver(loadingObserver)
operator fun <T> Data<T>.minusAssign(loadingObserver: LoadingObserver) = unregisterLoadingObserver(loadingObserver)

operator fun <T> Data<T>.plusAssign(errorObserver: ErrorObserver) = registerErrorObserver(errorObserver)
operator fun <T> Data<T>.minusAssign(errorObserver: ErrorObserver) = unregisterErrorObserver(errorObserver)