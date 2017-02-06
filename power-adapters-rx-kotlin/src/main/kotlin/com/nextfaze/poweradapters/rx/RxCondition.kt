package com.nextfaze.poweradapters

import com.nextfaze.poweradapters.rx.RxCondition
import rx.Observable

fun Condition.value(): Observable<Boolean> = RxCondition.value(this)

fun Condition.toObservable(): Observable<Boolean> = value()

fun Observable<Boolean>.toCondition(): Condition = RxCondition.observableCondition(this)