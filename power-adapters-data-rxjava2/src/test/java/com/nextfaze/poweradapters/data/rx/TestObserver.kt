package com.nextfaze.poweradapters.data.rx

import com.nextfaze.poweradapters.DataObserver
import com.nextfaze.poweradapters.data.AvailableObserver
import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.data.ErrorObserver
import com.nextfaze.poweradapters.data.LoadingObserver
import java.util.*

/**
 * An observer designed for testing [Data] objects.
 *
 * * Ensures that notifications emitted by the specified [Data] are consistent with its [Data.size]
 * return value.
 */
internal class TestObserver<T>(val data: Data<T>) : DataObserver, LoadingObserver, AvailableObserver, ErrorObserver {

    private var shadowSize = data.size()

    private val loadingValues = mutableListOf<Boolean>()

    private val availableValues = mutableListOf<Int>()

    private val changeEvents = mutableListOf<List<T>>()

    private val errors = mutableListOf<Throwable>()

    override fun onChanged() {
        shadowSize = data.size()
        addChangeEvent()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = addChangeEvent()

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        shadowSize += itemCount
        addChangeEvent()
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        shadowSize -= itemCount
        addChangeEvent()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = addChangeEvent()

    private fun addChangeEvent() {
        changeEvents += ArrayList(data.asList())
    }

    override fun onLoadingChange() {
        loadingValues += data.isLoading
    }

    override fun onAvailableChange() {
        availableValues += data.available()
    }

    override fun onError(e: Throwable) {
        errors += e
    }

    fun assertLoadingValues(vararg expected: Boolean): TestObserver<T> {
        assert(expected.toList() == loadingValues) {
            junit.framework.Assert.format("Loading values do not match", expected.toList(), loadingValues)
        }
        return this
    }

    fun assertAvailableValues(vararg expected: Int): TestObserver<T> {
        assert(expected.toList() == availableValues) {
            junit.framework.Assert.format("Available values do not match", expected.toList(), availableValues)
        }
        return this
    }

    fun assertChangeEvents(vararg expected: List<T>): TestObserver<T> {
        assert(expected.toList() == changeEvents) {
            junit.framework.Assert.format("Change events do not match", expected.toList(), changeEvents)
        }
        return this
    }

    fun assertErrors(vararg expected: Throwable): TestObserver<T> {
        assert(expected.toList() == errors) {
            junit.framework.Assert.format("Errors do not match", expected.toList(), errors)
        }
        return this
    }

    fun assertNotificationsConsistent(): TestObserver<T> {
        val actualSize = data.size()
        if (shadowSize != actualSize) {
            throw AssertionError("Inconsistency detected: expected size $shadowSize but it is $actualSize")
        }
        return this
    }

    fun assertNoErrors(): TestObserver<T> {
        assert(errors.isEmpty()) { "Expected no errors, but received $errors" }
        return this
    }
}

private inline fun assert(value: Boolean, lazyMessage: () -> Any) {
    if (!value) {
        val message = lazyMessage()
        throw AssertionError(message.toString())
    }
}

internal fun <T> Data<T>.test(): TestObserver<T> {
    val o = TestObserver(this)
    registerLoadingObserver(o)
    registerAvailableObserver(o)
    registerErrorObserver(o)
    registerDataObserver(o)
    o.assertNotificationsConsistent()
    return o
}
