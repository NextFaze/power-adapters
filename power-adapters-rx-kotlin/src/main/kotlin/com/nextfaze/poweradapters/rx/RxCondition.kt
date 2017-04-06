package com.nextfaze.poweradapters.rx

import com.nextfaze.poweradapters.Condition
import rx.Observable

fun Condition.value(): Observable<Boolean> = RxCondition.value(this)

fun Condition.toObservable(): Observable<Boolean> = value()

fun Observable<Boolean>.toCondition(): Condition = RxCondition.observableCondition(this)