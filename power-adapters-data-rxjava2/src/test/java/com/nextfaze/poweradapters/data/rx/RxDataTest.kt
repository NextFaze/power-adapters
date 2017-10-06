package com.nextfaze.poweradapters.data.rx

import com.nextfaze.poweradapters.data.FakeData
import com.nextfaze.poweradapters.data.rxjava2.BuildConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
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

    @Test fun loading() {
        val testObserver = RxData.loading(data).test()
        data.isLoading = true
        data.isLoading = false
        testObserver.assertNotTerminated()
        testObserver.assertValues(false, true, false)
    }

    @Test fun available() {
        data.setAvailable(0)
        val testObserver = RxData.available(data).test()
        data.setAvailable(5)
        testObserver.assertNotTerminated()
        testObserver.assertValues(0, 5)
    }
}
