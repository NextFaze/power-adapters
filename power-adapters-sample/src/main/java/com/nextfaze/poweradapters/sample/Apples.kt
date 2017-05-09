package com.nextfaze.poweradapters.sample

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.AssetManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.nextfaze.poweradapters.binder
import com.nextfaze.poweradapters.data.Data
import com.nextfaze.poweradapters.data.DataBindingAdapter
import com.nextfaze.poweradapters.data.cursorData
import com.nextfaze.poweradapters.plus
import com.nextfaze.poweradapters.sample.Utils.emptyMessage
import com.nextfaze.poweradapters.sample.Utils.loadingIndicator
import kotlinx.android.synthetic.main.apple_view.view.*
import java.io.File
import java.io.File.createTempFile
import kotlin.properties.Delegates.observable

private const val VERSION = 1

data class Apple(
        val name: String,
        val image: String?,
        val origin: String?,
        val firstDeveloped: String?,
        val comment: String?,
        val use: String?
)

class AppleDatabase(context: Context) {

    private val helper by lazy {
        val file = createTempFile("apples", ".sqlite")
        context.assets.copyToFile("apples.sqlite", file)
        object : SQLiteOpenHelper(context, file.absolutePath, null, VERSION) {
            override fun onCreate(db: SQLiteDatabase) {
            }

            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            }
        }
    }

    private val db by lazy { helper.readableDatabase }

    fun apples() = slowQuery("SELECT * FROM apple")

    @SuppressLint("Recycle")
    private fun slowQuery(sql: String, vararg args: Any): Cursor {
        // Simulate slow query
        Thread.sleep(2000)
        return db.rawQuery(sql, args.map { it.toString() }.toTypedArray())!!
    }
}

fun appleData(database: AppleDatabase) = cursorData(database::apples, {
    Apple(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5))
})

private val binder = binder<Apple, AppleView>(R.layout.apple_item) { _, apple, _ -> this.apple = apple }

fun appleAdapter(data: Data<Apple>) = DataBindingAdapter(binder, data) + loadingIndicator(data) + emptyMessage(data)

class AppleView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var apple by observable<Apple?>(null) { _, _, new ->
        nameView.text = new?.name
        originView.text = new?.origin
    }

    init {
        inflate(context, R.layout.apple_view, this)
    }
}

private fun AssetManager.copyToFile(name: String, file: File) {
    file.outputStream().use { out -> open(name).use { it.copyTo(out) } }
}