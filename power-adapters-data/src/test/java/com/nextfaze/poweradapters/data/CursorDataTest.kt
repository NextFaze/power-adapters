package com.nextfaze.poweradapters.data

import android.database.ContentObserver
import android.database.Cursor
import com.google.common.truth.Truth.assertThat
import com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService
import com.nextfaze.poweradapters.DataObserver
import com.nextfaze.poweradapters.RowMapper
import com.nhaarman.mockito_kotlin.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.Thread.currentThread
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit.SECONDS

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class CursorDataTest {

    private lateinit var executor: ExecutorService

    @Before fun setUp() {
        executor = newDirectExecutorService()
    }

    @After fun tearDown() {
        executor.shutdownNow()
    }

    @Test fun cursorIsLoadedViaLoaderFunction() {
        val data = Data.fromCursor({ testCursor1() }, { mock<Any>() }, executor)
        data.registerMockDataObserver()
        assertThat(data.size()).isEqualTo(3)
    }

    @Test fun cursorLoaderFunctionIsInvokedUponRefresh() {
        val loader = mock<Callable<Cursor>> { on { call() } doReturn testCursor1() }
        val data = Data.fromCursor(loader, RowMapper { mock<Any>() }, executor)
        data.registerMockDataObserver()
        data.refresh()
        verify(loader, times(2)).call()
    }

    @Test fun oldCursorIsClosedAfterLoaderFunctionReturnsNewCursor() {
        val cursor1 = mock<Cursor>()
        val cursor2 = mock<Cursor>()
        val loader = mock<Callable<Cursor>> {
            on { call() } doReturn cursor1 doReturn cursor2
        }
        val data = Data.fromCursor(loader, RowMapper { mock<Any>() }, executor)
        data.registerMockDataObserver()
        data.refresh()
        verify(cursor1).close()
    }

    @Test fun cursorIsLoadedUsingExecutor() {
        val loader = ThreadRecordingLoader()
        val data = Data.fromCursor(loader, RowMapper { mock<Any>() }, executor)
        data.registerMockDataObserver()
        loader.assertThread(currentThread())
    }

    @Test fun dataSizeEqualsCursorRowCount() {
        val cursor = testCursor1()
        val data = Data.fromCursor({ cursor }, { mock<Any>() }, executor)
        data.registerMockDataObserver()
        assertThat(data.size()).isEqualTo(cursor.count)
    }

    @Test fun cursorIsPositionedBeforeRowMapping() {
        val cursor = mock<Cursor> { on { count } doReturn 5 }
        val data = Data.fromCursor({ cursor }, { mock<Any>() }, executor)
        data.registerMockDataObserver()
        data.get(3)
        verify(cursor).moveToPosition(3)
    }

    @Test fun notificationsAreConsistentWithCursorChanges() {
        var cursor = testCursor1()
        val data = Data.fromCursor({ cursor }, { mock<Any>() }, executor)
        val verifyingDataObserver = VerifyingDataObserver(data)
        data.registerDataObserver(verifyingDataObserver)
        cursor = testCursor2()
        data.refresh()
        verifyingDataObserver.assertSizeConsistent()
    }

    @Test fun contentObserverIsRegisteredWithCursorWhileDataIsObserved() {
        val cursor = mock<Cursor>()
        val data = Data.fromCursor({ cursor }, { mock<Any>() }, executor)
        val observer = mock<DataObserver>()
        data.registerDataObserver(observer)
        val captor = argumentCaptor<ContentObserver>()
        verify(cursor).registerContentObserver(captor.capture())
        data.unregisterDataObserver(observer)
        Robolectric.getForegroundThreadScheduler().advanceBy(5, SECONDS)
        verify(cursor).unregisterContentObserver(captor.firstValue)
    }

    @Test fun contentObserverIsRegisteredAgainWhenDataBecomesObserved() {
        val cursor1 = testCursor1()
        val cursor2 = mock<Cursor>()
        var cursor: Cursor = cursor1
        val data = Data.fromCursor({ cursor }, { mock<Any>() }, executor)
        val observer = data.registerMockDataObserver()
        data.unregisterDataObserver(observer)
        cursor = cursor2
        cursor1.dispatchChange()
        Robolectric.getForegroundThreadScheduler().advanceBy(5, SECONDS)
        data.registerMockDataObserver()
        Robolectric.getForegroundThreadScheduler().advanceBy(5, SECONDS)
        verify(cursor2).registerContentObserver(any())
    }

    @Test fun cursorIsReloadedAfterDataObservesContentObserverChange() {
        val cursor1 = testCursor1()
        val loader = mock<Callable<Cursor>> { on { call() } doReturn cursor1 doReturn testCursor2() }
        val data = Data.fromCursor(loader, RowMapper { mock<Any>() }, executor)
        data.registerMockDataObserver()
        // Need to pause and resume Robolectric foreground scheduler,
        // otherwise it will run posted runnables synchronously.
        // Obviously a real device never does this.
        Robolectric.getForegroundThreadScheduler().pause()
        cursor1.dispatchChange()
        Robolectric.getForegroundThreadScheduler().unPause()
        verify(loader, times(2)).call()
    }

    @Test fun cursorIsClosedAfterAllObserversUnregister() {
        val cursor = mock<Cursor>()
        val data = Data.fromCursor({ cursor }, { mock<Any>() }, executor)
        val observer = mock<DataObserver>()
        data.registerDataObserver(observer)
        data.unregisterDataObserver(observer)
        Robolectric.getForegroundThreadScheduler().advanceBy(5, SECONDS)
        verify(cursor).close()
    }
}

private fun Data<*>.registerMockDataObserver() = mock<DataObserver>().also { registerDataObserver(it) }

private fun testCursor1() = fakeCursor("id", "name") {
    row(1, "a")
    row(2, "b")
    row(3, "c")
}

private fun testCursor2() = fakeCursor("id", "name") {
    row(4, "d")
    row(5, "e")
}

private class ThreadRecordingLoader : Callable<Cursor> {

    @Volatile private var executingThread: Thread? = null

    override fun call(): Cursor {
        executingThread = currentThread()
        return testCursor1()
    }

    fun assertThread(thread: Thread) {
        assertThat(executingThread).isSameAs(thread)
    }
}