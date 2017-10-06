package com.nextfaze.poweradapters.rxjava2

import com.nextfaze.poweradapters.Condition
import io.reactivex.Observable

fun Condition.value(): Observable<Boolean> = RxCondition.value(this)

fun Condition.toObservable(): Observable<Boolean> = value()

fun Observable<Boolean>.toCondition(): Condition = RxCondition.observableCondition(this)
