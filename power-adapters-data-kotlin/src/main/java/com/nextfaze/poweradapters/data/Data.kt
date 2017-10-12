package com.nextfaze.poweradapters.data

import android.database.Cursor
import android.database.DataSetObserver
import com.nextfaze.poweradapters.DataObserver
import com.nextfaze.poweradapters.binding.Binder
import com.nextfaze.poweradapters.binding.Mapper
import java.util.concurrent.ExecutorService

/** Returns a [Data] containing the specified elements. */
fun <T> dataOf(vararg elements: T): Data<T> = ImmutableData.of(*elements)

/** Returns a [Data] containing the specified elements. */
fun <T> dataOf(elements: Iterable<T>): Data<T> = ImmutableData.of(elements)

/**
 * Creates a [Data] whose elements will be populated by invoking the specified loader function in a
 * worker thread with the specified [ExecutorService].
 * @param load The function to be invoked to load the list of elements.
 * @param executor The [ExecutorService] used to invoke the loader function.
 * @return A [Data] instance that will present the elements retrieved via the loader function.
 * @see Data.fromList
 */
fun <T> data(
        load: () -> List<T>,
        executor: ExecutorService = DataExecutors.defaultExecutor()
): Data<T> = Data.fromList(load, executor)

/**
 * Creates a [Data] that presents elements of a [Cursor] retrieved by invoking the specified loader
 * function in a worker thread with the specified [ExecutorService]. [T] instances are mapped using the
 * specified row mapper function.
 * The cursor's position will be pre-configured - callers don't need to set it.
 * <p>
 * The [Data] will respond to backend changes by invoking the loader function again, if the cursor
 * dispatches notifications to the registered [DataSetObserver]s.
 * <p>
 * Note that the returned [Data] manages the [Cursor] instances itself, and callers should
 * never close cursors returned by the loader function.
 * @param load The function to be invoked to load the cursor.
 * @param mapRow The function to be invoked to map rows from the [Cursor] to instances of [T].
 * @param executor The [ExecutorService] used to invoke the cursor loader function.
 * @return A [Data] instance that will present the cursor elements.
 * @see Cursor#registerDataSetObserver(DataSetObserver)
 * @see DataSetObserver
 */
fun <T> cursorData(
        load: () -> Cursor,
        mapRow: (Cursor) -> T,
        executor: ExecutorService = DataExecutors.defaultExecutor()
): Data<T> = Data.fromCursor(load, mapRow, executor)

/** Returns a [Data] containing the elements of this list. */
fun <T> List<T>.toData(): Data<T> = ImmutableData.of(this)

/** Alias for [Data.size]. */
val Data<*>.size get() = size()

/** Alias for [Data.available]. */
val Data<*>.available get() = available()

fun <T> Data<T>.toAdapter(binder: Binder<in T, *>) = DataBindingAdapter(binder, this)

fun <T> Data<T>.toAdapter(mapper: Mapper<in T>) = DataBindingAdapter(mapper, this)

/** @see Data.registerDataObserver */
operator fun <T> Data<T>.plusAssign(dataObserver: DataObserver) = registerDataObserver(dataObserver)

/** @see Data.unregisterDataObserver */
operator fun <T> Data<T>.minusAssign(dataObserver: DataObserver) = unregisterDataObserver(dataObserver)

/** @see Data.registerAvailableObserver */
operator fun <T> Data<T>.plusAssign(availableObserver: AvailableObserver) = registerAvailableObserver(availableObserver)

/** @see Data.unregisterAvailableObserver */
operator fun <T> Data<T>.minusAssign(availableObserver: AvailableObserver) = unregisterAvailableObserver(availableObserver)

/** @see Data.registerLoadingObserver */
operator fun <T> Data<T>.plusAssign(loadingObserver: LoadingObserver) = registerLoadingObserver(loadingObserver)

/** @see Data.unregisterLoadingObserver */
operator fun <T> Data<T>.minusAssign(loadingObserver: LoadingObserver) = unregisterLoadingObserver(loadingObserver)

/** @see Data.registerErrorObserver */
operator fun <T> Data<T>.plusAssign(errorObserver: ErrorObserver) = registerErrorObserver(errorObserver)

/** @see Data.unregisterErrorObserver */
operator fun <T> Data<T>.minusAssign(errorObserver: ErrorObserver) = unregisterErrorObserver(errorObserver)
