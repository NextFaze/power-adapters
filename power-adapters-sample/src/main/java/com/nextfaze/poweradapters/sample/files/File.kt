package com.nextfaze.poweradapters.sample.files

import io.reactivex.Observable
import java.util.*
import java.util.Collections.emptyList
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val MAX_FILE_COUNT = 20
private const val FAKE_DELAY = 500L
private const val DEFAULT_RANDOM_SEED = 1

data class File constructor(
        val name: String,
        val count: Int,
        val size: Long,
        val isDirectory: Boolean,
        private val randomSeed: Int
) {
    companion object {
        fun rootDir() = File("/", 25, 0, true, DEFAULT_RANDOM_SEED)

        fun createDir(name: String, fileCount: Int) = File(name, fileCount, 0, true, name.hashCode())

        fun createFile(name: String): File {
            val seed = name.hashCode()
            return File(name, 0, Random(seed.toLong()).nextInt(10).toLong(), false, seed)
        }
    }

    fun listFiles() = when {
        !isDirectory -> emptyList()
        else -> {
            // Fake sleep to pretend this is a slow disk I/O operation.
            Thread.sleep(FAKE_DELAY)
            generateRandomFiles()
        }
    }

    fun files(): Observable<List<File>> = when {
        !isDirectory -> Observable.just(emptyList())
        else -> Observable.just(generateRandomFiles()).delay(FAKE_DELAY, MILLISECONDS)
    }

    override fun toString() = name

    private fun generateRandomFiles(): List<File> {
        val files = ArrayList<File>()
        val random = Random(randomSeed.toLong())
        for (i in 0 until count) {
            if (shouldBeDir(random)) {
                files.add(createDir(randomName(random), randomFileCount(random)))
            } else {
                files.add(createFile(randomName(random)))
            }
        }
        return files
    }
}

private fun shouldBeDir(random: Random) = random.nextBoolean()

private fun randomName(random: Random): String {
    val b = StringBuilder()
    for (i in 0 until 5) {
        b.append(randomChar(random))
    }
    return b.toString()
}

private fun randomChar(random: Random) = ('a' + random.nextInt('z'.toInt() - 'a'.toInt()))

private fun randomFileCount(random: Random) = when {
    random.nextInt(3) == 0 -> 0
    else -> 1 + random.nextInt(MAX_FILE_COUNT - 1)
}
