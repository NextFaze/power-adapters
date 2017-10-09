package com.nextfaze.poweradapters.data.test

import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.internal.NotificationType
import com.nextfaze.poweradapters.internal.NotifyingArrayList

class FakeData<T> : Data<T>() {

    private val data = NotifyingArrayList<T>(dataObservable)

    var available = Data.UNKNOWN
        set(value) {
            if (value != field) {
                field = value
                notifyAvailableChanged()
            }
        }

    var loading = false
        set(value) {
            if (value != field) {
                field = value
                notifyLoadingChanged()
            }
        }

    var notificationType: NotificationType
        get() = data.notificationType
        set(notificationType) {
            data.notificationType = notificationType
        }

    override fun size(): Int = data.size

    override fun get(position: Int, flags: Int): T = data[position]

    fun add(t: T) = data.add(t)

    fun addAll(collection: Collection<T>) = data.addAll(collection)

    fun addAll(index: Int, collection: Collection<T>) = data.addAll(index, collection)

    fun clear() = data.clear()

    fun remove(t: T) = data.remove(t)

    operator fun set(index: Int, o: T): T = data.set(index, o)

    override fun invalidate() {
    }

    override fun refresh() {
    }

    override fun reload() {
    }

    override fun available() = available

    override fun isLoading() = loading

    override fun asList() = data

    fun insert(index: Int, vararg items: T) = data.addAll(index, listOf(*items))

    fun append(vararg items: T) = addAll(listOf(*items))

    fun change(index: Int, vararg items: T) = data.setAll(index, listOf(*items))

    fun remove(index: Int, count: Int) = data.remove(index, count)

    fun move(fromPosition: Int, toPosition: Int, itemCount: Int) = data.move(fromPosition, toPosition, itemCount)

    fun error(e: Throwable) {
        super.notifyError(e)
    }
}
