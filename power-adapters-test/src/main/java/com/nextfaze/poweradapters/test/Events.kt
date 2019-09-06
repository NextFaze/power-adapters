package com.nextfaze.poweradapters.test

sealed class Event
data class ChangeEvent(val position: Int, val count: Int, val payload: Any?) : Event()
data class InsertEvent(val position: Int, val count: Int) : Event()
data class RemoveEvent(val position: Int, val count: Int) : Event()
data class MoveEvent(val fromPosition: Int, val toPosition: Int, val count: Int) : Event()
