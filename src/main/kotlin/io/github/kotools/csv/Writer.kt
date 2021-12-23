package io.github.kotools.csv

import com.github.doyaaaaaken.kotlincsv.client.CsvWriter
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

/**
 * Writes records in a CSV file according to the given [configuration], or
 * throws a [CsvConfigurationException] when the [configuration] is invalid.
 */
@Throws(CsvConfigurationException::class)
public suspend fun csvWriter(configuration: Writer.() -> Unit): Unit =
    withContext(IO) { delegateCsvWriter(configuration) }

/**
 * Writes records in a CSV file according to the given [configuration], or
 * returns `null` when the [configuration] is invalid.
 */
public suspend fun csvWriterOrNull(configuration: Writer.() -> Unit): Unit? =
    withContext(IO) { delegateCsvWriterOrNull(configuration) }

/**
 * Writes records in a CSV file **asynchronously** according to the given
 * [configuration], or throws a [CsvConfigurationException] when the
 * [configuration] is invalid.
 */
@Throws(CsvConfigurationException::class)
public infix fun CoroutineScope.csvWriterAsync(
    configuration: Writer.() -> Unit
): Deferred<Unit> = async(IO) { delegateCsvWriter(configuration) }

/**
 * Writes records in a CSV file **asynchronously** according to the given
 * [configuration], or returns `null` when the [configuration] is invalid.
 */
public infix fun CoroutineScope.csvWriterOrNullAsync(
    configuration: Writer.() -> Unit
): Deferred<Unit?> = async(IO) { delegateCsvWriterOrNull(configuration) }

@Throws(CsvConfigurationException::class)
private inline fun delegateCsvWriter(configuration: Writer.() -> Unit) {
    val writer: Writer = Writer create configuration
    if (writer.file.isBlank()) invalidPropertyException(writer::file)
    if (writer.header.isEmpty()) invalidPropertyException(writer::header)
    if (writer.records.isEmpty()) invalidPropertyException(writer::records)
    writer()
}

private inline fun delegateCsvWriterOrNull(configuration: Writer.() -> Unit):
        Unit? {
    val writer: Writer = Writer create configuration
    return if (writer.isInvalid()) null else writer()
}

/** Configurable object responsible for writing records in a CSV file. */
public class Writer internal constructor() : Manager() {
    internal val records: List<List<String>> get() = mutableRecords
    private val csv: CsvWriter get() = csvWriter { delimiter = separator.value }
    private val mutableRecords: MutableList<List<String>> = mutableListOf()

    /** **Required** property for defining the [file] content's header. */
    public var header: Set<String> = emptySet()

    /**
     * **Optional** flag for overwriting the [file]'s content.
     *
     * Set to `true` by default.
     */
    public var overwrite: Boolean = true

    /** **Required** function that defines the records to write. */
    public infix fun records(configuration: Records.() -> Unit) {
        mutableRecords += (Records create configuration).values
    }

    internal operator fun invoke(): Unit? {
        val f: File = (loader getResourceFile "$folder$file")
            ?: getSystemFile()
            ?: return null
        val computedRecords: List<List<String?>> =
            records.map { List(header.size, it::getOrNull) }
        return if (overwrite) computedRecords writeWithHeaderIn f
        else csv.writeAll(computedRecords, f, !overwrite)
    }

    internal fun isInvalid(): Boolean =
        file.isBlank() || header.isEmpty() || mutableRecords.isEmpty()

    private fun getSystemFile(): File? {
        val url: URL = loader.baseUrl ?: return null
        val path = Path("${url.path}$folder")
        path.createDirectoryIfNotExists()
        return File("$path/$file")
    }

    private infix fun List<List<String?>>.writeWithHeaderIn(file: File) {
        val r: MutableList<List<String?>> = mutableListOf(header.toList())
        r += this
        csv.writeAll(r, file)
    }

    private fun Path.createDirectoryIfNotExists() {
        if (notExists()) {
            createDirectory()
            overwrite = true
        }
    }

    internal companion object : Factory<Writer>

    /** Configurable object responsible for defining the records to write. */
    public class Records internal constructor() {
        internal val values: List<List<String>> get() = records
        private val records: MutableList<List<String>> = mutableListOf()

        /** Adds the given [Iterable] as a record. */
        public operator fun Iterable<String>.unaryPlus() {
            records += toList()
        }

        internal companion object : Factory<Records>
    }
}
