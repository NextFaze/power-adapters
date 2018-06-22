package com.nextfaze.poweradapters.data.rxjava2

import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.rxjava2.internal.DiffList
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer

/**
 * Creates a `Data<T>` from [Observable] data sources.
 * @param contents Each emission of this `Observable` overwrites the elements of the data.
 * @param available The [Data.available] property will match the emissions of this observable, starting with
 * [Int.MAX_VALUE] until the first emission. If `null`, the resulting data will assume there are no more elements
 * available after the first emission of any of the content observables.
 * @param loading The [Data.isLoading] property will match the emissions of this observable, starting with `false` until
 * the first emission. If `null`, the resulting data considers itself in a loading state until the first emission
 * of the content observable.
 * @param diffStrategy The strategy used to detect changes in content.
 */
fun <T : Any> observableData(
        contents: (loadType: LoadType) -> Observable<out Collection<T>>,
        available: ((loadType: LoadType) -> Observable<Int>)? = null,
        loading: ((loadType: LoadType) -> Observable<Boolean>)? = null,
        diffStrategy: DiffStrategy<T> = DiffStrategy.CoarseGrained
): Data<T> = KObservableData(contents, available, loading, diffStrategy)

/** Indicates the reason [Data] is subscribing to an `Observable` data source. */
enum class LoadType {
    /** The [Data] is subscribing because at least one observer is registered. */
    IMPLICIT,
    /** The [Data] is subscribing because [Data.refresh] was invoked. */
    REFRESH,
    /** The [Data] is subscribing because [Data.reload] was invoked. */
    RELOAD
}

/** Determines how change detection is performed. */
@Suppress("unused")
sealed class DiffStrategy<out T : Any> {
    /** Basic change range detection is performed, often resulting in many unwanted changes. */
    object CoarseGrained : DiffStrategy<Nothing>()

    /** Detect fine-grained changes and, optionally, moves. */
    data class FineGrained<T : Any>(
            /** Function that evaluates whether two objects have the same identity. */
            val identityEqual: (a: T, b: T) -> Boolean,
            /** Function that evaluates whether two objects have the same contents. */
            val contentEqual: (a: T, b: T) -> Boolean = Any::equals,
            /** Sets whether the content diff engine should also detect item moves, as well as other changes. */
            val detectMoves: Boolean = true
    ) : DiffStrategy<T>()
}

fun <T : Any> Observable<out Collection<T>>.toData(diffStrategy: DiffStrategy<T> = DiffStrategy.CoarseGrained): Data<T> =
        observableData(contents = { this }, diffStrategy = diffStrategy)

private class KObservableData<T : Any>(
        private val contentsSupplier: (loadType: LoadType) -> Observable<out Collection<T>>,
        private val availableSupplier: ((loadType: LoadType) -> Observable<Int>)? = null,
        private val loadingSupplier: ((loadType: LoadType) -> Observable<Boolean>)? = null,
        diffStrategy: DiffStrategy<T>
) : Data<T>() {

    private val list = DiffList(
            dataObservable,
            (diffStrategy as? DiffStrategy.FineGrained<T>)?.identityEqual,
            (diffStrategy as? DiffStrategy.FineGrained<T>)?.contentEqual,
            (diffStrategy as? DiffStrategy.FineGrained<T>)?.detectMoves ?: false
    )
    private val disposables = CompositeDisposable()
    private var clear: Boolean = false
    private var loading: Boolean = false
    private var available = Integer.MAX_VALUE

    override fun onFirstDataObserverRegistered() {
        super.onFirstDataObserverRegistered()
        if (clear) clear()
        subscribeIfAppropriate(LoadType.IMPLICIT)
    }

    override fun onLastDataObserverUnregistered() {
        super.onLastDataObserverUnregistered()
        unsubscribe()
    }

    private fun subscribeIfAppropriate(loadType: LoadType) {
        if (dataObserverCount > 0 && disposables.size() <= 0) {
            val onNext = EMPTY_CONSUMER
            val onError = Consumer<Throwable>(::notifyError)

            // Content
            val contents = contentsSupplier(loadType).replay(1).refCount()
            disposables.add(
                    contents.switchMap { list.overwrite(it).toObservable<Collection<T>>() }.subscribe(onNext, onError)
            )

            // Loading
            val loading = loadingSupplier?.invoke(loadType) ?: contents
                    .onErrorResumeNext(Observable.empty())
                    .take(1)
                    .map { false }
                    .concatWith(Observable.just(false))
                    .startWith(true)
            disposables.add(loading.subscribe(Consumer<Boolean>(::setLoading), onError))

            // Available
            val available = availableSupplier?.invoke(loadType) ?: contents
                    .onErrorResumeNext(Observable.empty())
                    .take(1)
                    .map { 0 }
                    .startWith(Int.MAX_VALUE)
            disposables.add(available.subscribe(Consumer<Int>(::setAvailable), onError))
        }
    }

    private fun unsubscribe() = disposables.clear()

    private fun clear() {
        list.clear()
        setAvailable(Integer.MAX_VALUE)
        clear = false
    }

    private fun setLoading(loading: Boolean) {
        runOnUiThread {
            if (this.loading != loading) {
                this.loading = loading
                notifyLoadingChanged()
            }
        }
    }

    private fun setAvailable(available: Int) {
        runOnUiThread {
            if (this.available != available) {
                this.available = available
                notifyAvailableChanged()
            }
        }
    }

    override fun get(position: Int, flags: Int) = list[position]

    override fun size() = list.size()

    override fun isLoading() = loading

    override fun available() = available

    override fun invalidate() {
        unsubscribe()
        clear = true
    }

    override fun refresh() {
        unsubscribe()
        subscribeIfAppropriate(LoadType.REFRESH)
    }

    override fun reload() {
        clear()
        unsubscribe()
        subscribeIfAppropriate(LoadType.RELOAD)
    }
}

private val EMPTY_CONSUMER = Consumer<Any> {}
