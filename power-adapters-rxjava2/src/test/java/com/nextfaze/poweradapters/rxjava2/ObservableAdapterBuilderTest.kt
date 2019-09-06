@file:Suppress("IllegalIdentifier")

package com.nextfaze.poweradapters.rxjava2

import android.view.View
import com.google.common.truth.Truth.assertThat
import com.nextfaze.poweradapters.binding.Binder
import com.nextfaze.poweradapters.test.ChangeEvent
import com.nextfaze.poweradapters.test.InsertEvent
import com.nextfaze.poweradapters.test.MoveEvent
import com.nextfaze.poweradapters.test.RemoveEvent
import com.nextfaze.poweradapters.test.test
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Observable
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ObservableAdapterBuilderTest {

    private val context = RuntimeEnvironment.application

    @Before fun setUp() {
        RxAndroidPlugins.setMainThreadSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
    }

    @After fun tearDown() {
        RxAndroidPlugins.reset()
        RxJavaPlugins.reset()
    }

    @Test fun `adapter contents matches contents observable`() {
        val binder = mockBinder<String, View>()
        ObservableAdapterBuilder<String>(binder)
                .contents(Observable.just(
                        listOf("a", "b"),
                        listOf("b", "c"),
                        listOf("d", "e", "f")
                ))
                .build()
                .test()
                .bind(context)
        verify(binder).bindView(any(), eq("d"), any(), any(), any())
        verify(binder).bindView(any(), eq("e"), any(), any(), any())
        verify(binder).bindView(any(), eq("f"), any(), any(), any())
    }

    @Test fun `data content changes match prepends observable`() {
        val binder = mockBinder<String, View>()
        ObservableAdapterBuilder<String>(binder)
                .prepends(Observable.just(
                        listOf("x", "y"),
                        listOf("a"),
                        listOf("b")
                ))
                .build()
                .test()
                .bind(context)
        verify(binder).bindView(any(), eq("b"), any(), any(), any())
        verify(binder).bindView(any(), eq("a"), any(), any(), any())
        verify(binder).bindView(any(), eq("x"), any(), any(), any())
        verify(binder).bindView(any(), eq("y"), any(), any(), any())
    }

    @Test fun `data content changes match appends observable`() {
        val binder = mockBinder<String, View>()
        ObservableAdapterBuilder<String>(binder)
                .appends(Observable.just(
                        listOf("a"),
                        listOf("b", "c"),
                        listOf("d")
                ))
                .build()
                .test()
                .bind(context)
        verify(binder).bindView(any(), eq("a"), any(), any(), any())
        verify(binder).bindView(any(), eq("b"), any(), any(), any())
        verify(binder).bindView(any(), eq("c"), any(), any(), any())
        verify(binder).bindView(any(), eq("d"), any(), any(), any())
    }

    @Test fun `fine-grained notifications are dispatched if equality functions are supplied`() {
        ObservableAdapterBuilder<Item>(mockBinder())
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
                        ChangeEvent(0, 1, null),
                        MoveEvent(1, 0, 1),
                        RemoveEvent(0, 2),
                        RemoveEvent(0, 1)
                )

    }

    @Test fun `fine-grained move notifications are not dispatched if move detection is disabled`() {
        ObservableAdapterBuilder<Item>(mockBinder())
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

    @Test fun `data unsubscribes from observables when no observers`() {
        var activeSubscriptionCount = 0
        val content = Observable.just(listOf("a", "b"))
                .concatWith(Observable.never())
                .doOnLifecycle({ activeSubscriptionCount++ }, { activeSubscriptionCount-- })
        ObservableAdapterBuilder<String>(mockBinder())
                .contents(content)
                .prepends(content)
                .appends(content)
                .build()
                .test {
                    observing = false
                    assertThat(activeSubscriptionCount).isEqualTo(0)
                }
    }

    data class Item(val id: Int, val name: String)
}

private fun <T, V : View> mockBinder() = mock<Binder<T, V>>(defaultAnswer = RETURNS_DEEP_STUBS)
