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
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObservableDataTest {

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
        observableData(
                contents = {
                    Observable.just(
                            listOf("a", "b"),
                            listOf("b", "c"),
                            listOf("d", "e", "f")
                    )
                }
        ).test {
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

    @Test fun `data available matches available observable`() {
        observableData<String>(
                contents = { Observable.never() },
                available = { Observable.just(5) }
        ).test().assertAvailableValues(Int.MAX_VALUE, 5)
    }

    @Test fun `data loading matches loading observable`() {
        observableData<String>(
                contents = { Observable.never() },
                loading = { Observable.just(true, false, true) }
        ).test().assertLoadingValues(false, true, false, true)
    }

    @Test fun `data stops loading upon content error`() {
        observableData<String>(contents = { Observable.error(Exception()) })
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data is indicated as loading until first content emission if no loading observable specified`() {
        observableData(contents = { Observable.just<List<String>>(listOf("a")) })
                .test()
                .assertLoadingValues(false, true, false)
        observableData(contents = { Observable.just<List<String>>(emptyList()) })
                .test()
                .assertLoadingValues(false, true, false)
    }

    @Test fun `data ceases loading synchronously with first content emission if no loading observable specified`() {
        val testScheduler = TestScheduler()
        RxAndroidPlugins.setMainThreadSchedulerHandler { testScheduler }
        val items = listOf(Item(1, "a"), Item(2, "b"))
        observableData(
                contents = { Observable.just(items) },
                diffStrategy = DiffStrategy.FineGrained(
                        identityEqual = { a, b -> a.id == b.id },
                        contentEqual = { a, b -> a.name == b.name },
                        detectMoves = false
                )
        ).test {
            assertLoadingValues(false, true, false)
            assertElementValues(emptyList(), items)
            testScheduler.triggerActions()
            assertNoErrors()
        }
    }

    @Test fun `notifications are dispatched based on existing data buffer when contents subscribed`() {
        var items = listOf(Item(1, "a"))
        val data = observableData(
                contents = { Observable.defer { Observable.just(items) } },
                diffStrategy = DiffStrategy.FineGrained(
                        identityEqual = { a, b -> a.id == b.id },
                        contentEqual = { a, b -> a.name == b.name },
                        detectMoves = false
                )
        )
        data.test { observing = false }
        items = listOf(Item(1, "a"), Item(2, "b"))
        data.test { assertChangeNotifications(InsertEvent(position = 1, count = 1)) }
    }

    @Test fun `source observables are subscribed on calling thread`() {
        val mainThreadTestScheduler = TestScheduler()
        val ioTestScheduler = TestScheduler()
        val computationTestScheduler = TestScheduler()
        RxAndroidPlugins.setMainThreadSchedulerHandler { mainThreadTestScheduler }
        RxJavaPlugins.setIoSchedulerHandler { ioTestScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { computationTestScheduler }
        var isContentSubscribed = false
        var isLoadingSubscribed = false
        var isAvailableSubscribed = false
        observableData(
                contents = {
                    Observable.defer {
                        isContentSubscribed = true
                        Observable.just(listOf("a"))
                    }
                },
                loading = {
                    Observable.defer {
                        isLoadingSubscribed = true
                        Observable.just(true)
                    }
                },
                available = {
                    Observable.defer {
                        isAvailableSubscribed = true
                        Observable.just(0)
                    }
                }
        ).test()
        assertThat(isContentSubscribed).isTrue()
        assertThat(isLoadingSubscribed).isTrue()
        assertThat(isAvailableSubscribed).isTrue()
    }

    @Test fun `fine-grained notifications consistent when source content emits too fast`() {
        val computationTestScheduler = TestScheduler()
        RxJavaPlugins.setComputationSchedulerHandler { computationTestScheduler }
        val contents = PublishSubject.create<List<String>>()
        observableData(contents = { contents }).test {
            contents.onNext(listOf("a", "b"))
            computationTestScheduler.triggerActions()
            contents.onNext(listOf("b"))
            contents.onNext(listOf("b", "c"))
            computationTestScheduler.triggerActions()
            assertNotificationsConsistent()
        }
    }

    @Test
    fun `data available indicated as infinite until first content emission if no available observable specified`() {
        observableData(contents = { Observable.just(listOf("a")) })
                .test()
                .assertAvailableValues(Int.MAX_VALUE, 0)
    }

    @Test fun `fine-grained notifications are dispatched if fine-grained diff strategy is supplied`() {
        observableData(
                contents = {
                    Observable.just(
                            listOf(Item(1, "a"), Item(2, "b")),
                            listOf(Item(1, "x"), Item(2, "b"), Item(3, "c")),
                            listOf(Item(2, "b"), Item(1, "x"), Item(3, "c")),
                            listOf(Item(3, "c")),
                            listOf()
                    )
                },
                diffStrategy = DiffStrategy.FineGrained(
                        identityEqual = { a, b -> a.id == b.id },
                        contentEqual = { a, b -> a.name == b.name }
                )
        ).test().assertChangeNotifications(
                InsertEvent(0, 2),
                InsertEvent(2, 1),
                ChangeEvent(0, 1),
                MoveEvent(1, 0, 1),
                RemoveEvent(0, 2),
                RemoveEvent(0, 1)
        )
    }

    @Test fun `fine-grained move notifications are not dispatched if move detection is disabled`() {
        observableData(
                contents = {
                    Observable.just(
                            listOf(Item(1, "a"), Item(2, "b")),
                            listOf(Item(2, "b"), Item(1, "a"))
                    )
                },
                diffStrategy = DiffStrategy.FineGrained(
                        identityEqual = { a, b -> a.id == b.id },
                        contentEqual = { a, b -> a.name == b.name },
                        detectMoves = false
                )
        ).test().assertChangeNotifications(
                InsertEvent(0, 2),
                RemoveEvent(1, 1),
                InsertEvent(0, 1)
        )
    }

    @Test fun `data emits error from contents observable error`() {
        val throwable = Throwable()
        observableData<String>(contents = { Observable.error(throwable) })
                .test()
                .assertErrors(throwable)
    }

    @Test fun `data unsubscribes from observables when no observers`() {
        var activeSubscriptionCount = 0
        val content = Observable.just(listOf("a", "b"))
                .concatWith(Observable.never())
                .doOnLifecycle({ activeSubscriptionCount++ }, { activeSubscriptionCount-- })
        observableData(contents = { content }).test {
            observing = false
            assertThat(activeSubscriptionCount).isEqualTo(0)
        }
    }

    @Test fun `data invalidate clears upon next observation`() {
        val data = observableData(contents = { Observable.just(listOf("a")).concatWith(Observable.never()) })
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
        val data = observableData(
                contents = { observable },
                available = { Observable.empty() },
                loading = { Observable.empty() }
        )
        data.test()
        data.refresh()
        assertThat(subscribeCount).isEqualTo(2)
    }

    @Test fun `data reload clears then resubscribes to observables`() {
        var subscribeCount = 0
        val observable = Observable.defer { subscribeCount++; Observable.just(listOf("a")) }
        val data = observableData(
                contents = { observable },
                available = { Observable.empty() },
                loading = { Observable.empty() }
        )
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
        val data = observableData(contents = { observable })
        data.test()
        assertThat(subscribeCount).isEqualTo(1)
    }

    @Test fun `data passes implicit load type to observable functions when first observer registers`() {
        var capturedLoadType: LoadType? = null
        val data = observableData<String>(contents = { capturedLoadType = it; Observable.empty() })
        data.test()
        assertThat(capturedLoadType).isEqualTo(LoadType.IMPLICIT)
    }

    @Test fun `data passes refresh load type to observable functions when refresh invoked`() {
        var capturedLoadType: LoadType? = null
        val data = observableData<String>(contents = { capturedLoadType = it; Observable.empty() })
        data.test()
        data.refresh()
        assertThat(capturedLoadType).isEqualTo(LoadType.REFRESH)
    }

    @Test fun `data passes reload load type to observable functions when reload invoked`() {
        var capturedLoadType: LoadType? = null
        val data = observableData<String>(contents = { capturedLoadType = it; Observable.empty() })
        data.test()
        data.reload()
        assertThat(capturedLoadType).isEqualTo(LoadType.RELOAD)
    }

    private data class Item(val id: Int, val name: String)
}
