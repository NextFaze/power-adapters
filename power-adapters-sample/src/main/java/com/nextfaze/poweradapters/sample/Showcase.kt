package com.nextfaze.poweradapters.sample

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.jakewharton.rxbinding3.view.clicks
import com.nextfaze.poweradapters.AdapterBuilder
import com.nextfaze.poweradapters.PowerAdapter
import com.nextfaze.poweradapters.buildAdapter
import com.nextfaze.poweradapters.recyclerview.toRecyclerAdapter
import com.nextfaze.poweradapters.rxjava2.showOnlyWhile
import com.nextfaze.poweradapters.sample.apples.ApplesViewModel
import com.nextfaze.poweradapters.sample.apples.createApplesAdapter
import com.nextfaze.poweradapters.sample.cats.CatsViewModel
import com.nextfaze.poweradapters.sample.cats.createCatsAdapter
import com.nextfaze.poweradapters.sample.files.FileTreeViewModel
import com.nextfaze.poweradapters.sample.files.createFileTreeAdapter
import com.nextfaze.poweradapters.sample.news.MultiTypeViewModel
import com.nextfaze.poweradapters.sample.news.NewsViewModel
import com.nextfaze.poweradapters.sample.news.createMultiTypeAdapter
import com.nextfaze.poweradapters.sample.news.createNewsAdapter
import com.nextfaze.poweradapters.sample.rxjava.attached
import com.nextfaze.poweradapters.sample.rxjava.not
import com.nextfaze.poweradapters.sample.rxjava.takeWhile
import com.nextfaze.poweradapters.viewFactory
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.header_view.view.*
import kotlinx.android.synthetic.main.showcase_fragment.*
import kotlinx.collections.immutable.immutableSetOf
import kotlinx.collections.immutable.mutate
import kotlin.properties.Delegates.observable

class ShowcaseViewModel : ViewModel() {
    var sections = emptyList<Section>()

    private val collapsedSectionsSubject = BehaviorSubject.createDefault(immutableSetOf<String>())

    fun toggleSectionCollapsed(section: String) = mutate { it.toggle(section) }

    fun setAllSectionsCollapsed(allCollapsed: Boolean) = mutate {
        if (allCollapsed) {
            it.clear()
            it.addAll(sections.map { it.name })
        } else {
            it.clear()
        }
    }

    fun sectionCollapsed(section: String): Observable<Boolean> = collapsedSectionsSubject.map { section in it }

    private fun mutate(body: (MutableSet<String>) -> Unit) =
            collapsedSectionsSubject.onNext(collapsedSectionsSubject.value!!.mutate(body))
}

data class Section(val name: String, val createAdapter: () -> PowerAdapter)

class ShowcaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.showcase_activity)
        title = "Power Adapters"
    }
}

class ShowcaseFragment : Fragment() {

    private val sections = listOf(
            Section("Files") { createFileTreeAdapter(fileTreeViewModel) },
            Section("Cats") { createCatsAdapter(catsViewModel) },
            Section("Apples") { createApplesAdapter(applesViewModel) },
            Section("News") { createNewsAdapter(newsViewModel) },
            Section("Multi-Type") { createMultiTypeAdapter(multiTypeViewModel) }
    )

    private val showcaseViewModel by viewModels<ShowcaseViewModel>()
    private val fileTreeViewModel by viewModels<FileTreeViewModel>()
    private val catsViewModel by viewModels<CatsViewModel>()
    private val applesViewModel by viewModels<ApplesViewModel>()
    private val newsViewModel by viewModels<NewsViewModel>()
    private val multiTypeViewModel by viewModels<MultiTypeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.showcase_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showcaseViewModel.sections = sections
        recyclerView.adapter = buildAdapter {
            sections.forEach { section(it.name, it.createAdapter()) }
        }.toRecyclerAdapter()
    }

    @SuppressLint("AutoDispose", "CheckResult")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.showcase, menu)
        menu.findItem(R.id.expandAll).clicks().subscribe { showcaseViewModel.setAllSectionsCollapsed(false) }
        menu.findItem(R.id.collapseAll).clicks().subscribe { showcaseViewModel.setAllSectionsCollapsed(true) }
    }

    @SuppressLint("AutoDispose")
    private fun AdapterBuilder.section(name: String, adapter: PowerAdapter) {
        val sectionExpanded = !showcaseViewModel.sectionCollapsed(name)

        // Header
        +viewFactory<HeaderView>(R.layout.header_item) {
            label = name
            sectionExpanded.takeWhile(attached()).subscribe { expanded = it }
            setOnClickListener { showcaseViewModel.toggleSectionCollapsed(name) }
        }

        // Adapter
        +adapter.showOnlyWhile(sectionExpanded)
    }
}

class HeaderView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var label by observable<CharSequence?>(null) { _, _, label -> labelView.text = label }

    var expanded by observable(false) { _, _, expanded ->
        dropdownButton.rotation = if (expanded) 180f else 0f
    }

    init {
        inflate(context, R.layout.header_view, this)
        ViewCompat.setBackground(this, context.getDrawableForAttribute(R.attr.selectableItemBackground))
    }
}
