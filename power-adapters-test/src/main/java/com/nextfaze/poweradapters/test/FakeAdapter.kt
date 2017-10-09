package com.nextfaze.poweradapters.test

import android.view.View
import android.view.ViewGroup
import com.nextfaze.poweradapters.Container
import com.nextfaze.poweradapters.Holder
import com.nextfaze.poweradapters.PowerAdapter
import com.nextfaze.poweradapters.internal.NotificationType

class FakeAdapter @JvmOverloads constructor(private var itemCount: Int = 0) : PowerAdapter() {

    var notificationType = NotificationType.FINE

    override fun getItemViewType(position: Int): Any {
        assertWithinRange(position)
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        assertWithinRange(position)
        return PowerAdapter.NO_ID.toLong()
    }

    override fun isEnabled(position: Int): Boolean {
        assertWithinRange(position)
        return true
    }

    override fun newView(parent: ViewGroup, viewType: Any) = View(parent.context)

    override fun bindView(container: Container, view: View, holder: Holder) {
    }

    override fun hasStableIds() = false

    override fun getItemCount() = itemCount

    fun insert(positionStart: Int, itemCount: Int) {
        this.itemCount += itemCount
        notificationType.notifyItemRangeInserted(dataObservable, positionStart, itemCount)
    }

    fun append(itemCount: Int) {
        insert(this.itemCount, itemCount)
    }

    fun remove(positionStart: Int, itemCount: Int) {
        if (positionStart + itemCount > this.itemCount) {
            throw IndexOutOfBoundsException()
        }
        this.itemCount -= itemCount
        notificationType.notifyItemRangeRemoved(dataObservable, positionStart, itemCount)
    }

    fun change(positionStart: Int, itemCount: Int) {
        notificationType.notifyItemRangeChanged(dataObservable, positionStart, itemCount)
    }

    fun move(fromPosition: Int, toPosition: Int, itemCount: Int) {
        notificationType.notifyItemRangeMoved(dataObservable, fromPosition, toPosition, itemCount)
    }

    fun clear() = remove(0, itemCount)

    private fun assertWithinRange(position: Int) {
        if (position >= itemCount) throw IndexOutOfBoundsException()
    }
}
