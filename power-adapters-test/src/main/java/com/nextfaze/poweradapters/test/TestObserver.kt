package com.nextfaze.poweradapters.test

import android.content.Context
import android.view.ViewGroup
import android.widget.FrameLayout
import com.nextfaze.poweradapters.Container
import com.nextfaze.poweradapters.DataObserver
import com.nextfaze.poweradapters.PowerAdapter
import kotlin.properties.Delegates.observable

/** An observer designed for testing [PowerAdapter] objects. */
class TestObserver(val adapter: PowerAdapter) : DataObserver {

    private var shadowSize = adapter.itemCount

    private val changeEvents = mutableListOf<Event>()

    var observing by observable(false) { _, old, new ->
        if (old != new) {
            if (new) {
                adapter.registerDataObserver(this)
            } else {
                adapter.unregisterDataObserver(this)
            }
        }
    }

    override fun onChanged() {
        shadowSize = adapter.itemCount
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        changeEvents += ChangeEvent(positionStart, itemCount)
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        shadowSize += itemCount
        changeEvents += InsertEvent(positionStart, itemCount)
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        shadowSize -= itemCount
        changeEvents += RemoveEvent(positionStart, itemCount)
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        changeEvents += MoveEvent(fromPosition, toPosition, itemCount)
    }

    fun assertItemCount(expected: Int): TestObserver {
        verify(expected == adapter.itemCount) {
            format("Item count does not match", expected, adapter.itemCount)
        }
        return this
    }

    fun assertChangeNotifications(vararg expected: Event): TestObserver {
        verify(expected.toList() == changeEvents) {
            format("Change events do not match", expected.toList(), changeEvents)
        }
        return this
    }

    fun assertNotificationsConsistent(): TestObserver {
        val actualSize = adapter.itemCount
        if (shadowSize != actualSize) {
            throw AssertionError("Inconsistency detected: expected size $shadowSize but it is $actualSize")
        }
        return this
    }

    fun bind(context: Context): TestObserver {
        val parent = FrameLayout(context)
        val container = TestContainer(parent, adapter)
        for (position in 0 until adapter.itemCount) {
            val viewType = adapter.getItemViewType(position)
            val view = adapter.newView(parent, viewType)
            adapter.bindView(container, view, { position })
        }
        return this
    }
}

private class TestContainer(private val viewGroup: ViewGroup, private val adapter: PowerAdapter) : Container() {

    override fun scrollToPosition(position: Int) {
    }

    override fun getItemCount() = adapter.itemCount

    override fun getViewGroup() = viewGroup

    override fun getRootContainer() = this
}

fun PowerAdapter.test(body: TestObserver.() -> Unit = {}): TestObserver {
    val o = TestObserver(this)
    o.observing = true
    o.assertNotificationsConsistent()
    o.body()
    return o
}
