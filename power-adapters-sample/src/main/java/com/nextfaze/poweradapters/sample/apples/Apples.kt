package com.nextfaze.poweradapters.sample.apples

import android.annotation.SuppressLint
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.content.res.AssetManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import com.nextfaze.poweradapters.binder
import com.nextfaze.poweradapters.buildAdapter
import com.nextfaze.poweradapters.data.cursorData
import com.nextfaze.poweradapters.data.toAdapter
import com.nextfaze.poweradapters.sample.R
import com.nextfaze.poweradapters.sample.emptyMessage
import com.nextfaze.poweradapters.sample.loadingIndicator
import kotlinx.android.synthetic.main.apple_view.view.*
import java.io.File
import java.io.File.createTempFile
import kotlin.properties.Delegates.observable

private const val VERSION = 1

class ApplesViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppleDatabase(application)

    val data = cursorData(database::apples, {
        Apple(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getString(4), it.getString(5))
    })
}

data class Apple(
        val name: String,
        val image: String?,
        val origin: String?,
        val firstDeveloped: String?,
        val comment: String?,
        val use: String?
)

private class AppleDatabase(context: Context) {

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

private val binder = binder<Apple, AppleView>(R.layout.apple_item) { _, apple, _ -> this.apple = apple }

fun createApplesAdapter(viewModel: ApplesViewModel) = buildAdapter {
    +viewModel.data.toAdapter(binder)
    +loadingIndicator(viewModel.data)
    +emptyMessage(viewModel.data)
}

class AppleView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var apple by observable<Apple?>(null) { _, _, apple ->
        if (apple != null) {
            nameView.text = apple.name
            originView.text = apple.origin
        }
    }

    init {
        inflate(context, R.layout.apple_view, this)
    }
}

private fun AssetManager.copyToFile(name: String, file: File) {
    file.outputStream().use { out -> open(name).use { it.copyTo(out) } }
}
