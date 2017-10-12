package com.nextfaze.poweradapters.sample.cats

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.view.View
import com.jakewharton.rx.replayingShare
import com.nextfaze.poweradapters.PowerAdapter.asAdapter
import com.nextfaze.poweradapters.adapter
import com.nextfaze.poweradapters.binder
import com.nextfaze.poweradapters.data.rx.ObservableDataBuilder
import com.nextfaze.poweradapters.data.rx.availableChanges
import com.nextfaze.poweradapters.data.rx.loading
import com.nextfaze.poweradapters.data.toAdapter
import com.nextfaze.poweradapters.rxjava2.showOnlyWhile
import com.nextfaze.poweradapters.sample.R
import com.nextfaze.poweradapters.sample.rxjava.and
import com.nextfaze.poweradapters.sample.rxjava.not
import com.nextfaze.poweradapters.toAdapter
import com.nextfaze.poweradapters.viewFactory
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.plus
import kotlinx.collections.immutable.toImmutableList
import java.util.concurrent.TimeUnit

/** Asset path to the CSV file. */
private const val CSV_ASSET_PATH = "cats.csv"

private const val ARTIFICIAL_DELAY = 1L
private val ARTIFICIAL_DELAY_UNIT = TimeUnit.SECONDS

/** Number of cats retrieved per page. */
private const val PAGE_SIZE = 5

class CatsViewModel(application: Application) : AndroidViewModel(application) {

    /** Emits every load event, from "load next" clicks. */
    private val loads = PublishSubject.create<Any>()

    /** A paged list of cats. */
    private val contents = loads
            // Emit a sequence of int ranges
            .scan(0 until PAGE_SIZE) { range, _ -> range.next(PAGE_SIZE) }
            // Load each range of cats into a page, in order
            .concatMap{ cats(application, it).toObservable() }
            // Reduce pages into a single list
            .scan { list, page -> list + page }
            .observeOn(mainThread())
            // Maintain only a single subscription, which ensures the pages don't start from the beginning
            .replayingShare()

    // Subscribe to contents for the lifetime of the ViewModel to ensure pages don't start from the beginning
    private val disposable = contents.subscribe()

    val data = ObservableDataBuilder<Cat>()
            .contents(contents)
            // We're loading after each click, until the new content list comes through
            .loading(loads.map { true }.startWith(true).mergeWith(contents.map { false }))
            // Number of available elements is based on the total reported by the page
            .available(contents.map { it.total - it.size })
            // Cats are identified by name
            .identityEquality { a, b -> a.name == b.name }
            .build()

    /** Invoke this to trigger a load. */
    fun onLoadNextClick() = loads.onNext(Unit)

    override fun onCleared() = disposable.dispose()
}

/** Creates an adapter that presents cats specified by [viewModel]. */
fun createCatsAdapter(viewModel: CatsViewModel) = adapter {
    // Cat list
    val binder = binder<Cat, CatView>(R.layout.cat_binder_item) { _, cat, _ -> this.cat = cat }
    +viewModel.data.toAdapter(binder)

    // Load next button
    val loadNextVisible = !viewModel.data.loading() and viewModel.data.availableChanges().map { it > 0 }
    +viewFactory<View>(R.layout.list_load_next_item) {
        setOnClickListener { viewModel.onLoadNextClick() }
    }.toAdapter().showOnlyWhile(loadNextVisible)

    // Loading indicator
    +asAdapter(R.layout.list_loading_item).showOnlyWhile(viewModel.data.loading())
}

/** Emits Cat lists in the specified range from the CSV file. */
private fun cats(context: Context, range: IntRange): Single<Page<Cat>> = Single.fromCallable {
    readCatsFromCsv(context).page(range)
}.subscribeOn(Schedulers.io()).delay(ARTIFICIAL_DELAY, ARTIFICIAL_DELAY_UNIT)

// CSV columns: breed, country, origin, body type, coat, pattern
private fun readCatsFromCsv(context: Context) = context.assets.open(CSV_ASSET_PATH).bufferedReader()
        .useLines { it.map(::parseCat).toList().toImmutableList() }

internal fun parseCat(line: String) = line.split(',').take(2)
        // Trim whitespace and quotes, omitting empty tokens
        .map { it.trim { it.isWhitespace() || it == '\"' } }
        .let { Cat(it[0], it[1]) }

/** Return a sub list in the form of a [Page], which includes the total. Returns an empty list if out of bounds. */
private fun <T> ImmutableList<T>.page(range: IntRange) =
        Page(subList(range.start.coerceAtMost(size), (range.endInclusive + 1).coerceAtMost(size)), size)

internal fun IntRange.next(step: Int, total: Int = Int.MAX_VALUE): IntRange {
    require(step > 0) { "Step must be > 0, but is $step" }
    val start = endInclusive.coerceAtMost(total - 1) + 1
    return start until (start + step).coerceAtMost(total)
}

/** Meow. */
data class Cat(val name: String, val country: String)

/**
 * A sub list of a greater collection of [T] elements.
 * @property total The total number of elements in the greater collection.
 */
private data class Page<T>(private val contents: ImmutableList<T>, val total: Int) : ImmutableList<T> by contents {
    operator fun plus(page: Page<T>) = Page(contents + page.contents, total)
}
