package com.nextfaze.poweradapters.rxjava2

import androidx.annotation.CheckResult
import com.nextfaze.poweradapters.Condition
import io.reactivex.Observable

fun Condition.value(): Observable<Boolean> = RxCondition.value(this)

@CheckResult fun Condition.toObservable(): Observable<Boolean> = value()

@CheckResult fun Observable<Boolean>.toCondition(): Condition = RxCondition.observableCondition(this)
