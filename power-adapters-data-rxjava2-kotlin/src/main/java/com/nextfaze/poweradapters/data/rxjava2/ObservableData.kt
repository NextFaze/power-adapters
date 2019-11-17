package com.nextfaze.poweradapters.data.rxjava2

import androidx.annotation.CheckResult
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import com.nextfaze.poweradapters.data.Data
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.MainThreadDisposable.verifyMainThread
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlin.math.min

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
 * @param diffComputation Determines which thread is used to compute diffs. Changing this to
 * [DiffComputation.Synchronous] may be useful for ensuring change notifications happen synchronously,
 * which can facilitate certain animations.
 * Uses [DiffComputation.Asynchronous] by default.
 */
@CheckResult fun <T : Any> observableData(
        contents: (loadType: LoadType) -> Observable<out Collection<T>>,
        available: ((loadType: LoadType) -> Observable<Int>)? = null,
        loading: ((loadType: LoadType) -> Observable<Boolean>)? = null,
        diffStrategy: DiffStrategy<T> = DiffStrategy.CoarseGrained,
        diffComputation: DiffComputation = DiffComputation.Asynchronous()
): Data<T> = KObservableData(contents, available, loading, diffStrategy, diffComputation)

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
    /** No change detection is performed. Change callbacks still occur, but item animations likely won't work. */
    object None : DiffStrategy<Nothing>()

    /** Basic change range detection is performed, often resulting in many unwanted changes. */
    object CoarseGrained : DiffStrategy<Nothing>()

    /** Detect fine-grained changes and, optionally, moves. */
    data class FineGrained<T : Any>(
            /** Function that evaluates whether two objects have the same identity. */
            val identityEqual: (a: T, b: T) -> Boolean,
            /** Function that evaluates whether two objects have the same contents. */
            val contentEqual: (a: T, b: T) -> Boolean = Any::equals,
            /** Computes change payload metadata. */
            val changePayload: (a: T, b: T) -> Any? = { _, _ -> null },
            /** Sets whether the content diff engine should also detect item moves, as well as other changes. */
            val detectMoves: Boolean = true
    ) : DiffStrategy<T>()
}

@CheckResult fun <T : Any> Observable<out Collection<T>>.toData(
        diffStrategy: DiffStrategy<T> = DiffStrategy.CoarseGrained,
        diffComputation: DiffComputation = DiffComputation.Asynchronous()
): Data<T> = observableData(
        contents = { this },
        diffStrategy = diffStrategy,
        diffComputation = diffComputation
)

/** Determines the thread used to compute diffs. */
sealed class DiffComputation {
    /** Compute diffs on the current thread. If used, the source [Observable] **must** emit on the main thread. */
    object Synchronous : DiffComputation()

    /** Compute diffs on a worker thread specified by [scheduler]. */
    class Asynchronous(val scheduler: Scheduler = Schedulers.computation()) : DiffComputation()
}

private class KObservableData<T : Any>(
        private val contentsSupplier: (loadType: LoadType) -> Observable<out Collection<T>>,
        private val availableSupplier: ((loadType: LoadType) -> Observable<Int>)? = null,
        private val loadingSupplier: ((loadType: LoadType) -> Observable<Boolean>)? = null,
        private val diffStrategy: DiffStrategy<T>,
        private val diffComputation: DiffComputation
) : Data<T>() {

    private var list = emptyList<T>()
    private val disposables = CompositeDisposable()
    private var clear: Boolean = false
    private var loading: Boolean = false
    private var available = Integer.MAX_VALUE

    private val listUpdateCallback = object : ListUpdateCallback {
        override fun onChanged(position: Int, count: Int, payload: Any?) =
                notifyItemRangeChanged(position, count, payload)

        override fun onMoved(fromPosition: Int, toPosition: Int) =
                notifyItemMoved(fromPosition, toPosition)

        override fun onInserted(position: Int, count: Int) =
                notifyItemRangeInserted(position, count)

        override fun onRemoved(position: Int, count: Int) =
                notifyItemRangeRemoved(position, count)
    }

    private val computeChangeDispatch: ComputeChangeDispatch<T> =
            when (diffStrategy) {
                is DiffStrategy.None -> ::computeChangeDispatchNone
                is DiffStrategy.CoarseGrained -> ::computeChangeDispatchCoarse
                is DiffStrategy.FineGrained -> { oldList, newList ->
                    computeChangeDispatchFine(
                            detectMoves = diffStrategy.detectMoves,
                            oldList = oldList,
                            newList = newList
                    )
                }
            }

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
            val content = contentsSupplier(loadType).replay(1).refCount()

            // Content
            val changes = content
                    .map { (it as? List<T>) ?: it.toList() }
                    .startWith(list)
                    .buffer(2, 1)
                    // Filter out the final buffer emission, which may not be of size 2
                    .filter { it.size == 2 }
                    .concatMapMaybe { (prev, next) ->
                        computeChangeDispatch(prev, next).map { dispatch -> Change(next, dispatch) }
                    }
                    .run {
                        when (diffComputation) {
                            // Apply immediately
                            is DiffComputation.Synchronous -> this
                            // Apply in sequence on main thread
                            is DiffComputation.Asynchronous -> observeOn(mainThread())
                        }
                    }
            disposables.add(changes.subscribe(::applyChange, ::notifyError))

            // Loading
            val loading = loadingSupplier?.invoke(loadType) ?: content
                    .onErrorResumeNext(Observable.empty())
                    .take(1)
                    .map { false }
                    .concatWith(Observable.just(false))
                    .startWith(true)
            disposables.add(loading.subscribe(::setLoading, ::notifyError))

            // Available
            val available = availableSupplier?.invoke(loadType) ?: content
                    .onErrorResumeNext(Observable.empty())
                    .take(1)
                    .map { 0 }
                    .startWith(Int.MAX_VALUE)
            disposables.add(available.subscribe(::setAvailable, ::notifyError))
        }
    }

    private fun unsubscribe() = disposables.clear()

    private fun clear() {
        val size = list.size
        if (size > 0) {
            list = emptyList()
            notifyItemRangeRemoved(0, size)
        }
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

    override fun size() = list.size

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

    private fun diffUtilCallback(
            oldList: List<T>,
            newList: List<T>
    ) = object : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                (diffStrategy as DiffStrategy.FineGrained).identityEqual(oldList[oldItemPosition], newList[newItemPosition])

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                (diffStrategy as DiffStrategy.FineGrained).contentEqual(oldList[oldItemPosition], newList[newItemPosition])

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) =
                (diffStrategy as DiffStrategy.FineGrained).changePayload(oldList[oldItemPosition], newList[newItemPosition])

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size
    }

    @Suppress("UNUSED_PARAMETER")
    private fun computeChangeDispatchNone(
            oldList: List<T>,
            newList: List<T>
    ): Maybe<ChangeDispatch<T>> = Maybe.just<ChangeDispatch<T>> { notifyDataSetChanged() }

    private fun computeChangeDispatchCoarse(
            oldList: List<T>,
            newList: List<T>
    ): Maybe<ChangeDispatch<T>> {
        val oldSize = oldList.size
        val newSize = newList.size
        val deltaSize = newSize - oldSize
        return Maybe.just<ChangeDispatch<T>> {
            if (deltaSize < 0) {
                notifyItemRangeRemoved(oldSize + deltaSize, -deltaSize)
            } else if (deltaSize > 0) {
                notifyItemRangeInserted(oldSize, deltaSize)
            }
            val changed = min(oldSize, newSize)
            if (changed > 0) notifyItemRangeChanged(0, changed, null)
        }
    }

    private fun computeChangeDispatchFine(
            detectMoves: Boolean,
            oldList: List<T>,
            newList: List<T>
    ): Maybe<ChangeDispatch<T>> = when {
        // Same list instances, so skip the worker thread.
        newList === oldList -> Maybe.empty()
        // Different list instances
        else -> when {
            // Both empty, no change.
            oldList.isEmpty() && newList.isEmpty() -> Maybe.empty()
            // Insert initial list.
            oldList.isEmpty() && newList.isNotEmpty() -> Maybe.fromCallable<ChangeDispatch<T>> {
                return@fromCallable { notifyItemRangeInserted(0, newList.size) }
            }
            // Delete entire existing list.
            oldList.isNotEmpty() && newList.isEmpty() -> Maybe.fromCallable<ChangeDispatch<T>> {
                return@fromCallable { notifyItemRangeRemoved(0, oldList.size) }
            }
            // Different lists, so compute the change on a worker thread.
            else -> Maybe.fromCallable<ChangeDispatch<T>> {
                val diffResult = DiffUtil.calculateDiff(diffUtilCallback(oldList = oldList, newList = newList), detectMoves)
                return@fromCallable { diffResult.dispatchUpdatesTo(listUpdateCallback) }
            }
        }.run {
            when (diffComputation) {
                // Compute on current thread.
                is DiffComputation.Synchronous -> this
                // Compute on specified scheduler.
                is DiffComputation.Asynchronous -> subscribeOn(diffComputation.scheduler)
            }
        }
    }

    private fun applyChange(change: Change<T>) {
        verifyMainThread()
        list = change.list
        change.dispatch(this)
    }

    private data class Change<T : Any>(
            val list: List<T>,
            val dispatch: ChangeDispatch<T> = {}
    )
}

private typealias ComputeChangeDispatch<T> = (oldList: List<T>, newList: List<T>) -> Maybe<ChangeDispatch<T>>
private typealias ChangeDispatch<T> = KObservableData<T>.() -> Unit
