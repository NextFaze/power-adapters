package com.nextfaze.poweradapters.data.test

import com.nextfaze.poweradapters.DataObserver
import com.nextfaze.poweradapters.data.AvailableObserver
import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.data.ErrorObserver
import com.nextfaze.poweradapters.data.LoadingObserver
import com.nextfaze.poweradapters.test.ChangeEvent
import com.nextfaze.poweradapters.test.Event
import com.nextfaze.poweradapters.test.InsertEvent
import com.nextfaze.poweradapters.test.MoveEvent
import com.nextfaze.poweradapters.test.RemoveEvent
import com.nextfaze.poweradapters.test.format
import com.nextfaze.poweradapters.test.verify
import kotlin.properties.Delegates.observable

/**
 * An observer designed for testing [Data] objects.
 *
 * * Ensures that notifications emitted by the specified [Data] are consistent with its [Data.size]
 * return value.
 */
class TestDataObserver<T>(val data: Data<T>) : DataObserver, LoadingObserver, AvailableObserver, ErrorObserver {

    private var shadowSize = data.size()

    private val loadingValues = mutableListOf<Boolean>()

    private val availableValues = mutableListOf<Int>()

    private val elementValues = mutableListOf<List<T>>()

    private val errors = mutableListOf<Throwable>()

    private val changeEvents = mutableListOf<Event>()

    var observing by observable(false) { _, old, new ->
        if (old != new) {
            if (new) {
                data.registerLoadingObserver(this)
                data.registerAvailableObserver(this)
                data.registerErrorObserver(this)
                data.registerDataObserver(this)
            } else {
                data.unregisterLoadingObserver(this)
                data.unregisterAvailableObserver(this)
                data.unregisterErrorObserver(this)
                data.unregisterDataObserver(this)
            }
        }
    }

    init {
        loadingValues += data.isLoading
        availableValues += data.available()
        addElementValues()
    }

    override fun onChanged() {
        shadowSize = data.size()
        addElementValues()
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        addElementValues()
        changeEvents += ChangeEvent(positionStart, itemCount)
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        shadowSize += itemCount
        addElementValues()
        changeEvents += InsertEvent(positionStart, itemCount)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        shadowSize -= itemCount
        addElementValues()
        changeEvents += RemoveEvent(positionStart, itemCount)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        addElementValues()
        changeEvents += MoveEvent(fromPosition, toPosition, itemCount)
    }

    private fun addElementValues() {
        elementValues += data.asList().toList()
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

    fun assertLoadingValues(vararg expected: Boolean): TestDataObserver<T> {
        verify(expected.toList() == loadingValues) {
            format("Loading values do not match", expected.toList(), loadingValues)
        }
        return this
    }

    fun assertAvailableValues(vararg expected: Int): TestDataObserver<T> {
        verify(expected.toList() == availableValues) {
            format("Available values do not match", expected.toList(), availableValues)
        }
        return this
    }

    fun assertElementValues(vararg expected: Iterable<T>): TestDataObserver<T> {
        verify(expected.toList() == elementValues) {
            format("Element values do not match", expected.toList(), elementValues)
        }
        return this
    }

    fun assertElements(vararg expected: T): TestDataObserver<T> {
        verify(expected.toList() == data.asList()) {
            format("Elements do not match", expected.toList(), data.asList())
        }
        return this
    }

    fun assertChangeNotifications(vararg expected: Event): TestDataObserver<T> {
        verify(expected.toList() == changeEvents) {
            format("Change events do not match", expected.toList(), changeEvents)
        }
        return this
    }

    fun assertErrors(vararg expected: Throwable): TestDataObserver<T> {
        verify(expected.toList() == errors) { format("Errors do not match", expected.toList(), errors) }
        return this
    }

    fun assertNoErrors(): TestDataObserver<T> {
        verify(errors.isEmpty()) { "Expected no errors, but received $errors" }
        return this
    }

    fun assertSize(expected: Int): TestDataObserver<T> {
        verify(expected == data.size()) { format("Size does not match", expected, data.size()) }
        return this
    }

    fun assertNotificationsConsistent(): TestDataObserver<T> {
        val actualSize = data.size()
        if (shadowSize != actualSize) {
            throw AssertionError("Inconsistency detected: expected size $shadowSize but it is $actualSize")
        }
        return this
    }
}

fun <T> Data<T>.test(body: TestDataObserver<T>.() -> Unit = {}): TestDataObserver<T> {
    val o = TestDataObserver(this)
    o.observing = true
    o.assertNotificationsConsistent()
    o.body()
    return o
}
