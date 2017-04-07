package com.nextfaze.poweradapters

fun dataObserver(body: () -> Unit): DataObserver = object : SimpleDataObserver() {
    override fun onChanged() = body()
}