package com.nextfaze.poweradapters.sample.files

import android.arch.lifecycle.ViewModel
import com.jakewharton.rx.replayingShare
import com.nextfaze.poweradapters.PowerAdapter
import com.nextfaze.poweradapters.adapter
import com.nextfaze.poweradapters.binder
import com.nextfaze.poweradapters.data.rxjava2.ObservableDataBuilder
import com.nextfaze.poweradapters.data.toAdapter
import com.nextfaze.poweradapters.rxjava2.showOnlyWhile
import com.nextfaze.poweradapters.sample.R
import com.nextfaze.poweradapters.sample.emptyMessage
import com.nextfaze.poweradapters.sample.files.FileTreeViewModel.Flag.EXPANDED
import com.nextfaze.poweradapters.sample.files.FileTreeViewModel.Flag.PEEKING
import com.nextfaze.poweradapters.sample.loadingIndicator
import com.nextfaze.poweradapters.sample.nest
import com.nextfaze.poweradapters.sample.toggle
import com.nextfaze.poweradapters.toAdapter
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.immutableMapOf
import kotlinx.collections.immutable.immutableSetOf
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.toImmutableSet

class FileTreeViewModel : ViewModel() {
    /** Root directory to present files from. */
    val root = File.rootDir()

    /** Maintains the set of [Flag]s for each [File]. */
    private val fileToFlagSetSubject =
            BehaviorSubject.createDefault<ImmutableMap<File, ImmutableSet<Flag>>>(immutableMapOf())

    /** Effectively caches directory content listing. */
    private val dirContentsCache = mutableMapOf<File, Observable<List<File>>>()

    /** Enables/disables [PEEKING] for [file], also clearing it from other files. */
    fun togglePeeking(file: File) {
        // Stop peeking at all other files
        mutate { fileToFlagSet ->
            fileToFlagSet.entries.forEach {
                if (it.key != file) {
                    it.setValue(it.value.filter { it != PEEKING }.toImmutableSet())
                }
            }
        }
        // Toggle peeking at this file
        toggle(file, PEEKING)
    }

    /** Enables/disables [flag] for [file]. */
    fun toggle(file: File, flag: Flag) = mutate {
        it[file] = it.getOrPut(file) { immutableSetOf() }.mutate { it.toggle(flag) }
    }

    /** Emits if [file] has [flag] currently enabled. */
    fun isSet(file: File, flag: Flag): Observable<Boolean> = fileToFlagSetSubject.map { flag in it[file].orEmpty() }

    /** Contents of a directory. */
    fun files(file: File): Observable<List<File>> = dirContentsCache.getOrPut(file) { file.files().replayingShare() }

    private fun mutate(body: (MutableMap<File, ImmutableSet<Flag>>) -> Unit) =
            fileToFlagSetSubject.onNext(fileToFlagSetSubject.value!!.mutate(body))

    /** Enum of possible flags each [File] in a [FileTree] can have.  */
    enum class Flag { EXPANDED, PEEKING }
}

/** Creates an adapter that presents [viewModel]. */
fun createFileTreeAdapter(viewModel: FileTreeViewModel) = createFileTreeAdapter(viewModel.root, viewModel, 0)

private fun createFileTreeAdapter(
        file: File,
        viewModel: FileTreeViewModel,
        depth: Int
): PowerAdapter = adapter {
    // Binds a file to a view
    val fileBinder = binder<File, FileView>(R.layout.file_tree_file_item) { container, f, holder ->
        setFile(f)
        setDepth(depth)
        isClickable = f.isDirectory
        setOnClickListener {
            if (f.isDirectory) viewModel.toggle(f, EXPANDED)
            container.scrollToPosition(holder.position)
        }
        setOnPeekListener { if (f.isDirectory) viewModel.togglePeeking(f) }
    }

    // File listing
    val files = viewModel.files(file).share()
    val data = ObservableDataBuilder<File>()
            .contents(files)
            // Compare attributes that determine identity
            .identityEquality { a, b -> a.name == b.name }
            // Compare attributes that affect presentation
            .contentEquality { a, b -> a.name == b.name && a.size == b.size }
            .build()
    +data.toAdapter(fileBinder).nest {
        adapter {
            val f = data[it]
            // Single-item peek adapter
            +peekAdapter(f, viewModel)
            // If directory, the file adapter representing the contents
            if (f.isDirectory) {
                +createFileTreeAdapter(f, viewModel, depth + 1).showOnlyWhile(viewModel.isSet(f, EXPANDED))
            }
        }
    }

    // Loading indicator
    +loadingIndicator(files)

    // Empty message
    +emptyMessage(files)
}

/** Creates an adapter that presents a (fake) preview row for the contents of [file]. */
private fun peekAdapter(file: File, viewModel: FileTreeViewModel): PowerAdapter {
    val peekBinder = binder<File, FilePeekView>(R.layout.file_tree_peek_item) { _, f, _ -> setFile(f) }
    return listOf(file).toAdapter(peekBinder).showOnlyWhile(viewModel.isSet(file, PEEKING))
}


