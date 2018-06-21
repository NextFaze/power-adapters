@file:Suppress("IllegalIdentifier")

package com.nextfaze.poweradapters.data

import com.google.common.util.concurrent.MoreExecutors.newDirectExecutorService
import com.nextfaze.poweradapters.data.test.test
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

import java.util.concurrent.ExecutorService

@RunWith(RobolectricTestRunner::class)
class ArrayDataTest {

    private lateinit var executor: ExecutorService

    @Before fun setUp() {
        executor = newDirectExecutorService()
    }

    @After fun tearDown() {
        executor.shutdownNow()
    }

    @Test fun `change and removal combo notifications match reported size`() {
        val initialContents = listOf("foo", "bar", "baz")
        val minusOneElement = listOf("foo", "baz")
        var contents = initialContents
        val data = Data.fromList({ contents }, executor)
        val testDataObserver = data.test()
        contents = minusOneElement
        data.refresh()
        testDataObserver.assertNotificationsConsistent()
    }
}

