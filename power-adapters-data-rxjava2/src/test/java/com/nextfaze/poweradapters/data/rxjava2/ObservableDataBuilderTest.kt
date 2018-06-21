@file:Suppress("IllegalIdentifier")

package com.nextfaze.poweradapters.data.rxjava2

import com.google.common.truth.Truth.assertThat
import com.nextfaze.poweradapters.data.test.test
import com.nextfaze.poweradapters.test.ChangeEvent
import com.nextfaze.poweradapters.test.InsertEvent
import com.nextfaze.poweradapters.test.MoveEvent
import com.nextfaze.poweradapters.test.RemoveEvent
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers.trampoline
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObservableDataBuilderTest {

    @Before fun setUp() {
        RxAndroidPlugins.setMainThreadSchedulerHandler { trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { trampoline() }
    }

    @After fun tearDown() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test fun `data contents matches contents observable emissions`() {
        ObservableDataBuilder<String>()
                .contents(Observable.just(
                        listOf("a", "b"),
                        listOf("b", "c"),
                        listOf("d", "e", "f")
                ))
                .build()
                .test {
                    assertElementValues(
                            listOf(),
                            listOf("a", "b"),
                            listOf("b", "c"),
                            listOf("d", "e", "f"),
                            listOf("d", "e", "f")
                    )
                    assertElements("d", "e", "f")
                }
    }

    @Test fun `data content changes match prepends observable emissions`() {
        ObservableDataBuilder<String>()
                .prepends(Observable.just(
                        listOf("x", "y"),
                        listOf("a"),
                        listOf("b")
                ))
                .build()
                .test()
                .assertElementValues(
                        listOf(),
                        listOf("x", "y"),
                        listOf("a", "x", "y"),
                        listOf("b", "a", "x", "y")
                )
    }

    @Test fun `data content changes match appends observable emissions`() {
        ObservableDataBuilder<String>()
                .appends(Observable.just(
                        listOf("a"),
                        listOf("b", "c"),
                        listOf("d")
                ))
                .build()
                .test()
                .assertElementValues(
                        listOf(),
                        listOf("a"),
                        listOf("a", "b", "c"),
                        listOf("a", "b", "c", "d")
                )
    }

    @Test fun `data available matches available observable`() {
        ObservableDataBuilder<String>()
                .available(Observable.just(5))
                .build()
                .test()
                .assertAvailableValues(Int.MAX_VALUE, 5)
    }

    @Test fun `data loading matches loading observable`() {
        ObservableDataBuilder<String>()
                .loading(Observable.just(true, false, true))
                .build()
                .test()
                .assertLoadingValues(false, true, false, true)
    }

    @Test fun `data stops loading upon content error`() {
        ObservableDataBuilder<String>()
                .contents(Observable.error(Exception()))
                .build()
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data stops loading upon prepends error`() {
        ObservableDataBuilder<String>()
                .appends(Observable.error(Exception()))
                .build()
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data stops loading upon appends error`() {
        ObservableDataBuilder<String>()
                .prepends(Observable.error(Exception()))
                .build()
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data emits same errors as errors observable`() {
        val errors = listOf(Throwable(), Throwable(), Throwable())
        ObservableDataBuilder<String>()
                .errors(Observable.fromIterable(errors))
                .build()
                .test()
                .assertErrors(*errors.toTypedArray())
    }

    @Test fun `data is indicated as loading until first content emission if no loading observable specified`() {
        ObservableDataBuilder<String>()
                .contents(Observable.just(listOf("a")))
                .build()
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data is indicated as loading until first prepends emission if no loading observable specified`() {
        ObservableDataBuilder<String>()
                .prepends(Observable.just(listOf("a")))
                .build()
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data is indicated as loading until first appends emission if no loading observable specified`() {
        ObservableDataBuilder<String>()
                .appends(Observable.just(listOf("a")))
                .build()
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test
    fun `data available indicated as infinite until first content emission if no available observable specified`() {
        ObservableDataBuilder<String>()
                .contents(Observable.just(listOf("a")))
                .build()
                .test()
                .assertAvailableValues(Int.MAX_VALUE, 0)
    }

    @Test
    fun `data available indicated as infinite until first prepends emission if no available observable specified`() {
        ObservableDataBuilder<String>()
                .prepends(Observable.just(listOf("a")))
                .build()
                .test()
                .assertAvailableValues(Int.MAX_VALUE, 0)
    }

    @Test
    fun `data available indicated as infinite until first appends emission if no available observable specified`() {
        ObservableDataBuilder<String>()
                .appends(Observable.just(listOf("a")))
                .build()
                .test()
                .assertAvailableValues(Int.MAX_VALUE, 0)
    }

    @Test fun `fine-grained notifications are dispatched if equality functions are supplied`() {
        ObservableDataBuilder<Item>()
                .contents(Observable.just(
                        listOf(Item(1, "a"), Item(2, "b")),
                        listOf(Item(1, "x"), Item(2, "b"), Item(3, "c")),
                        listOf(Item(2, "b"), Item(1, "x"), Item(3, "c")),
                        listOf(Item(3, "c")),
                        listOf()
                ))
                .identityEquality { a, b -> a.id == b.id }
                .contentEquality { a, b -> a.name == b.name }
                .build()
                .test()
                .assertChangeNotifications(
                        InsertEvent(0, 2),
                        InsertEvent(2, 1),
                        ChangeEvent(0, 1),
                        MoveEvent(1, 0, 1),
                        RemoveEvent(0, 2),
                        RemoveEvent(0, 1)
                )
    }

    @Test fun `fine-grained move notifications are not dispatched if move detection is disabled`() {
        ObservableDataBuilder<Item>()
                .contents(Observable.just(
                        listOf(Item(1, "a"), Item(2, "b")),
                        listOf(Item(2, "b"), Item(1, "a"))
                ))
                .identityEquality { a, b -> a.id == b.id }
                .contentEquality { a, b -> a.name == b.name }
                .detectMoves(false)
                .build()
                .test()
                .assertChangeNotifications(
                        InsertEvent(0, 2),
                        RemoveEvent(1, 1),
                        InsertEvent(0, 1)
                )
    }

    @Test fun `data emits error from contents observable error`() {
        val throwable = Throwable()
        ObservableDataBuilder<Item>()
                .contents(Observable.error(throwable))
                .build()
                .test()
                .assertErrors(throwable)
    }

    @Test fun `data emits error from prepends observable error`() {
        val throwable = Throwable()
        ObservableDataBuilder<Item>()
                .prepends(Observable.error(throwable))
                .build()
                .test()
                .assertErrors(throwable)
    }

    @Test fun `data emits error from appends observable error`() {
        val throwable = Throwable()
        ObservableDataBuilder<Item>()
                .appends(Observable.error(throwable))
                .build()
                .test()
                .assertErrors(throwable)
    }

    @Test fun `data unsubscribes from observables when no observers`() {
        var activeSubscriptionCount = 0
        val content = Observable.just(listOf("a", "b"))
                .concatWith(Observable.never())
                .doOnLifecycle({ activeSubscriptionCount++ }, { activeSubscriptionCount-- })
        ObservableDataBuilder<String>()
                .contents(content)
                .prepends(content)
                .appends(content)
                .build()
                .test {
                    observing = false
                    assertThat(activeSubscriptionCount).isEqualTo(0)
                }
    }

    @Test fun `data invalidate clears upon next observation`() {
        val data = ObservableDataBuilder<String>()
                .contents(Observable.just(listOf("a")).concatWith(Observable.never()))
                .build()
        val testDataObserver = data.test()
        testDataObserver.observing = false
        data.invalidate()
        testDataObserver.observing = true
        testDataObserver.assertElementValues(
                listOf(),
                listOf("a"),
                listOf(),
                listOf("a")
        )
        testDataObserver.assertAvailableValues(Int.MAX_VALUE, 0, Int.MAX_VALUE, 0)
    }

    @Test fun `data refresh resubscribes to observables`() {
        var subscribeCount = 0
        val observable = Observable.defer { subscribeCount++; Observable.just(listOf("a")) }
        val data = ObservableDataBuilder<String>()
                .contents(observable)
                .available(Observable.empty())
                .loading(Observable.empty())
                .build()
        data.test()
        data.refresh()
        assertThat(subscribeCount).isEqualTo(2)
    }

    @Test fun `data reload clears then resubscribes to observables`() {
        var subscribeCount = 0
        val observable = Observable.defer { subscribeCount++; Observable.just(listOf("a")) }
        val data = ObservableDataBuilder<String>()
                .contents(observable)
                .available(Observable.empty())
                .loading(Observable.empty())
                .build()
        val testDataObserver = data.test()
        data.reload()
        assertThat(subscribeCount).isEqualTo(2)
        testDataObserver.assertElementValues(
                listOf(),
                listOf("a"),
                listOf(),
                listOf("a")
        )
    }

    @Test fun `data subscribes to content observable only once`() {
        var subscribeCount = 0
        val observable = Observable.defer { subscribeCount++; Observable.just(listOf("a")).concatWith(Observable.never()) }
        val data = ObservableDataBuilder<String>()
                .contents(observable)
                .build()
        data.test()
        assertThat(subscribeCount).isEqualTo(1)
    }

    data class Item(val id: Int, val name: String)
}
