package com.nextfaze.poweradapters.data.rxjava2

import com.nextfaze.poweradapters.data.test.FakeData
import com.nextfaze.poweradapters.rxjava2.ChangeEvent
import com.nextfaze.poweradapters.rxjava2.InsertEvent
import com.nextfaze.poweradapters.rxjava2.MoveEvent
import com.nextfaze.poweradapters.rxjava2.RemoveEvent
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class RxDataTest {

    private lateinit var data: FakeData<String>

    @Before fun setUp() {
        data = FakeData()
    }

    @Test fun size() {
        val testObserver = RxData.size(data).test()
        data.add("a")
        data.addAll(listOf("b", "c"))
        data.remove("a")
        testObserver.assertNotTerminated().assertValues(0, 1, 3, 2)
    }

    @Test fun changes() {
        val testObserver = RxData.changes(data).test()
        data.add("a")
        data[0] = "b"
        testObserver.assertNotTerminated().assertValues(ChangeEvent(0, 1))
    }

    @Test fun inserts() {
        val testObserver = RxData.inserts(data).test()
        data.add("a")
        testObserver.assertNotTerminated().assertValues(InsertEvent(0, 1))
    }

    @Test fun removes() {
        val testObserver = RxData.removes(data).test()
        data.add("a")
        data.remove("a")
        testObserver.assertNotTerminated().assertValues(RemoveEvent(0, 1))
    }

    @Test fun moves() {
        val testObserver = RxData.moves(data).test()
        data.addAll(listOf("a", "b", "c"))
        data.move(1, 2, 1)
        testObserver.assertNotTerminated().assertValues(MoveEvent(1, 2, 1))
    }

    @Test fun loading() {
        val testObserver = RxData.loading(data).test()
        data.loading = true
        data.loading = false
        testObserver.assertNotTerminated().assertValues(false, true, false)
    }

    @Test fun available() {
        data.available = 0
        val testObserver = RxData.available(data).test()
        data.available = 5
        testObserver.assertNotTerminated().assertValues(0, 5)
    }

    @Test fun errors() {
        data.error(RuntimeException())
        val testObserver = RxData.errors(data).test()
        val e1 = RuntimeException()
        val e2 = RuntimeException()
        data.error(e1)
        data.error(e2)
        testObserver.assertNotTerminated().assertValues(e1, e2)
    }
}
