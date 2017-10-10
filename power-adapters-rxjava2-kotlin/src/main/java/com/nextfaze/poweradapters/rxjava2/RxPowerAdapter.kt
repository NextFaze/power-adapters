package com.nextfaze.poweradapters.rxjava2

import com.nextfaze.poweradapters.PowerAdapter
import io.reactivex.Observable

fun PowerAdapter.itemCount(): Observable<Int> = RxPowerAdapter.itemCount(this)

fun PowerAdapter.changes(): Observable<ChangeEvent> = RxPowerAdapter.changes(this)

fun PowerAdapter.inserts(): Observable<InsertEvent> = RxPowerAdapter.inserts(this)

fun PowerAdapter.removes(): Observable<RemoveEvent> = RxPowerAdapter.removes(this)

fun PowerAdapter.moves(): Observable<MoveEvent> = RxPowerAdapter.moves(this)

fun PowerAdapter.showOnlyWhile(observable: Observable<Boolean>): PowerAdapter = showOnlyWhile(observable.toCondition())
