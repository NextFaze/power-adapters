package com.nextfaze.poweradapters.data.rx

import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.rx.ChangeEvent
import com.nextfaze.poweradapters.rx.InsertEvent
import com.nextfaze.poweradapters.rx.MoveEvent
import com.nextfaze.poweradapters.rx.RemoveEvent
import rx.Observable

fun Data<*>.sizeChanges(): Observable<Int> = RxData.size(this)

fun <T> Data<T>.elements(): Observable<Data<T>> = RxData.elements(this)

fun Data<*>.changes(): Observable<ChangeEvent> = RxData.changes(this)

fun Data<*>.inserts(): Observable<InsertEvent> = RxData.inserts(this)

fun Data<*>.removes(): Observable<RemoveEvent> = RxData.removes(this)

fun Data<*>.moves(): Observable<MoveEvent> = RxData.moves(this)

fun Data<*>.loading(): Observable<Boolean> = RxData.loading(this)

fun Data<*>.availableChanges(): Observable<Int> = RxData.available(this)

fun Data<*>.errors(): Observable<Throwable> = RxData.errors(this)