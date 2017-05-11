package com.nextfaze.poweradapters.data

import android.database.MatrixCursor

fun fakeCursor(vararg columnNames: String, body: FakeCursorBuilder.() -> Unit = {}) =
        FakeCursorBuilder(columnNames.asIterable()).apply(body).build()

class FakeCursorBuilder(columnNames: Iterable<String>) {

    private val c = TestCursor(columnNames)

    fun row(vararg columnValues: Any?) {
        c.newRow().apply { columnValues.forEach { add(it) } }
    }

    fun build(): TestCursor = c
}

class TestCursor(columnsNames: Iterable<String>) : MatrixCursor(columnsNames.toList().toTypedArray()) {
    fun dispatchChange() = onChange(false)
}