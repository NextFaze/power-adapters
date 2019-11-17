package com.nextfaze.poweradapters.sample.rxjava

import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import com.jakewharton.rxbinding3.view.ViewAttachAttachedEvent
import com.jakewharton.rxbinding3.view.attachEvents
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.rxkotlin.Observables

fun <T> Observable<T>.log(
        tag: String,
        includeEmissionValues: Boolean = false,
        prefix: String = ""
): Observable<T> = doOnSubscribe { Log.d(tag, "${prefix}Subscribe") }
        .doOnDispose { Log.d(tag, "${prefix}Dispose") }
        .doOnComplete { Log.d(tag, "${prefix}Complete") }
        .doOnError { e -> Log.d(tag, "${prefix}Error", e) }
        .run {
            when {
                includeEmissionValues -> doOnNext { v -> Log.d(tag, "${prefix}Next: " + v) }
                else -> doOnNext { Log.d(tag, "${prefix}Next") }
            }
        }

/** Emit changes in attached state, starting with the initial value. */
fun View.attached(): Observable<Boolean> = Observable.defer {
    Observable.just(ViewCompat.isAttachedToWindow(this))
            .concatWith(attachEvents().map { it is ViewAttachAttachedEvent })
}

/** Mirror the source `Observable` while the specified `Observable`'s latest emitted value is true. */
fun <T> Observable<T>.takeWhile(p: Observable<Boolean>): Observable<T> {
    val shared = p.distinctUntilChanged().share()
    return shared.filter { it }.switchMap { takeUntil(shared.filter { !it }) }
}

operator fun Flowable<Boolean>.not(): Flowable<Boolean> = map { !it }
infix fun Flowable<Boolean>.and(o: Flowable<Boolean>) = expr(o) { a, b -> a && b }
infix fun Flowable<Boolean>.or(o: Flowable<Boolean>) = expr(o) { a, b -> a || b }

operator fun Observable<Boolean>.not(): Observable<Boolean> = map { !it }
infix fun Observable<Boolean>.and(o: Observable<Boolean>) = expr(o) { a, b -> a && b }
infix fun Observable<Boolean>.or(o: Observable<Boolean>) = expr(o) { a, b -> a || b }

private inline fun Flowable<Boolean>.expr(
        o: Flowable<Boolean>,
        crossinline expression: (Boolean, Boolean) -> Boolean
):
        Flowable<Boolean> = Flowables.combineLatest(this, o) { a, b -> expression(a, b) }.distinctUntilChanged()

private inline fun Observable<Boolean>.expr(
        o: Observable<Boolean>,
        crossinline expression: (Boolean, Boolean) -> Boolean
): Observable<Boolean> = Observables.combineLatest(this, o) { a, b -> expression(a, b) }.distinctUntilChanged()
