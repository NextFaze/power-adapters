package com.nextfaze.poweradapters.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.nextfaze.poweradapters.*

class ShowcaseFragment : BaseFragment() {

    private val catData by lazy { Cats.createData(context) }

    private val appleData by lazy { appleData(AppleDatabase(context.applicationContext)) }

    private val rootFile = File.rootDir()

    private val rootData = DirectoryData(rootFile)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onDestroy() {
        catData.close()
        super.onDestroy()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setAdapter(adapter {
            section("Files", FileTree.createAdapter(rootData, rootFile))
            section("Cats", Cats.createAdapter(catData))
            section("Apples", appleAdapter(appleData))
        })
        setDatas(rootData, catData)
    }

    private fun AdapterBuilder.section(title: String, adapter: PowerAdapter) {
        val expanded = ValueCondition(true)
        view(ViewFactory {
            (LayoutInflater.from(it.context).inflate(R.layout.header_item, it, false) as TextView).apply {
                text = title
                setOnClickListener { expanded.set(!expanded.get()) }
            }
        })
        adapter(adapter.showOnlyWhile(expanded))
    }
}
