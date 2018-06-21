package com.nextfaze.poweradapters.rxjava2

import com.nextfaze.poweradapters.test.FakeAdapter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class RxPowerAdapterTest {

    private lateinit var adapter: FakeAdapter

    @Before fun setUp() {
        adapter = FakeAdapter()
    }

    @Test fun itemCount() {
        val testObserver = RxPowerAdapter.itemCount(adapter).test()
        adapter.append(1)
        adapter.append(2)
        adapter.remove(0, 1)
        testObserver.assertNotTerminated().assertValues(0, 1, 3, 2)
    }

    @Test fun changes() {
        val testObserver = RxPowerAdapter.changes(adapter).test()
        adapter.append(1)
        adapter.change(0, 1)
        testObserver.assertNotTerminated().assertValues(ChangeEvent(0, 1))
    }

    @Test fun inserts() {
        val testObserver = RxPowerAdapter.inserts(adapter).test()
        adapter.append(1)
        testObserver.assertNotTerminated().assertValues(InsertEvent(0, 1))
    }

    @Test fun removes() {
        val testObserver = RxPowerAdapter.removes(adapter).test()
        adapter.append(1)
        adapter.remove(0, 1)
        testObserver.assertNotTerminated().assertValues(RemoveEvent(0, 1))
    }

    @Test fun moves() {
        val testObserver = RxPowerAdapter.moves(adapter).test()
        adapter.append(3)
        adapter.move(1, 2, 1)
        testObserver.assertNotTerminated().assertValues(MoveEvent(1, 2, 1))
    }
}
