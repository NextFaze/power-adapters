@file:Suppress("IllegalIdentifier")

package com.nextfaze.poweradapters.rxjava2

import com.google.common.truth.Truth.assertThat
import com.nextfaze.poweradapters.Condition
import com.nextfaze.poweradapters.Observer
import com.nextfaze.poweradapters.ValueCondition
import com.nhaarman.mockito_kotlin.mock
import io.reactivex.Observable
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class RxConditionTest {
    @Test fun `condition value observable emits correct values`() {
        val condition = ValueCondition()
        val testObserver = RxCondition.value(condition).test()
        condition.set(true)
        testObserver.assertNotTerminated().assertValues(false, true)
    }

    @Test fun `condition value observable unregisters when subscription disposed`() {
        val condition = mock<Condition>()
        val testObserver = RxCondition.value(condition).test()
        testObserver.dispose()
        verify(condition).unregisterObserver(any())
    }

    @Test fun `observable condition first value is observed`() {
        val condition = RxCondition.observableCondition(Observable.just(true))
        condition.registerObserver(mock(Observer::class.java))
        assertThat(condition.eval()).isTrue()
    }

    @Test fun `observable condition subsequent values are observed`() {
        val condition = RxCondition.observableCondition(Observable.just(false, true))
        condition.registerObserver(mock(Observer::class.java))
        assertThat(condition.eval()).isTrue()
    }

    @Test fun `observable condition values are dispatched`() {
        val condition = RxCondition.observableCondition(Observable.just(true, false, true, true))
        val observer = mock(Observer::class.java)
        condition.registerObserver(observer)
        verify(observer, times(3)).onChanged()
        verifyNoMoreInteractions(observer)
    }
}
