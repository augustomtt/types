package io.github.kotools.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.reflect.KClass

/**
 * Returns the file's records according to the given [configuration], or throws
 * a [CsvConfigurationException] when the [configuration] is invalid or when the
 * targeted file doesn't exist.
 */
@Throws(CsvConfigurationException::class)
public suspend fun csvReader(configuration: Reader.() -> Unit):
        List<Map<String, String>> =
    withContext(IO) { delegateCsvReader(configuration) }

/**
 * Returns the file's records **asynchronously** according to the given
 * [configuration], or throws a [CsvConfigurationException] when the
 * [configuration] is invalid or when the targeted file doesn't exist.
 */
public infix fun CoroutineScope.csvReaderAsync(
    configuration: Reader.() -> Unit
): Deferred<List<Map<String, String>>> =
    async(IO) { delegateCsvReader(configuration) }

/**
 * Returns the file's records according to the given [configuration], or returns
 * `null` when the [configuration] is invalid or when the targeted file doesn't
 * exist.
 */
public suspend fun csvReaderOrNull(configuration: Reader.() -> Unit):
        List<Map<String, String>>? =
    withContext(IO) { delegateCsvReaderOrNull(configuration) }

/**
 * Returns the file's records as a given list of type [T] according to the given
 * [configuration].
 * This method returns `null` when the type [T] is not a public or internal
 * data class, or when the [configuration] is invalid, or when the targeted file
 * doesn't exist.
 */
@Suppress("DEPRECATION")
public suspend inline fun <reified T : Any> csvReaderOrNullAs(
    noinline configuration: Reader.() -> Unit
): List<T>? = csvReaderOrNullAs(T::class, configuration)

@Deprecated(
    message = "Use the `csvReaderOrNullAs<T> {}` method instead.",
    ReplaceWith("csvReaderOrNullAs<T> {}")
)
public suspend fun <T : Any> csvReaderOrNullAs(
    type: KClass<T>,
    configuration: Reader.() -> Unit
): List<T>? = withContext(IO) { delegateCsvReaderOrNullAs(type, configuration) }

/**
 * Returns the file's records as a given list of type [T] **asynchronously**
 * according to the given [configuration].
 * This method returns `null` when the type [T] is not a public or internal
 * data class, or when the [configuration] is invalid, or when the targeted file
 * doesn't exist.
 */
@Suppress("DEPRECATION")
public inline infix fun <reified T : Any> CoroutineScope.csvReaderOrNullAsAsync(
    noinline configuration: Reader.() -> Unit
): Deferred<List<T>?> = csvReaderOrNullAsAsync(T::class, configuration)

@Deprecated(
    message = "Use the `csvReaderOrNullAsAsync<T> {}` method instead.",
    ReplaceWith("csvReaderOrNullAsAsync<T> {}")
)
public fun <T : Any> CoroutineScope.csvReaderOrNullAsAsync(
    type: KClass<T>,
    configuration: Reader.() -> Unit
): Deferred<List<T>?> =
    async(IO) { delegateCsvReaderOrNullAs(type, configuration) }

/**
 * Returns the file's records **asynchronously** according to the given
 * [configuration], or returns `null` when the [configuration] is invalid or
 * when the targeted file doesn't exist.
 */
public infix fun CoroutineScope.csvReaderOrNullAsync(
    configuration: Reader.() -> Unit
): Deferred<List<Map<String, String>>?> =
    async(IO) { delegateCsvReaderOrNull(configuration) }

@Throws(CsvConfigurationException::class)
private inline fun delegateCsvReader(configuration: Reader.() -> Unit):
        List<Map<String, String>> {
    val reader: Reader = Reader create configuration
    if (reader.file.isBlank()) invalidPropertyException(reader::file)
    return reader() ?: fileNotFoundException("${reader.folder}${reader.file}")
}

private inline fun delegateCsvReaderOrNull(configuration: Reader.() -> Unit):
        List<Map<String, String>>? {
    val reader: Reader = Reader create configuration
    return if (reader.file.isBlank()) null else reader()
}

private inline fun <T : Any> delegateCsvReaderOrNullAs(
    type: KClass<T>,
    configuration: Reader.() -> Unit
): List<T>? {
    val dataType: DataType<T> = type.toDataTypeOrNull() ?: return null
    return delegateCsvReaderOrNull(configuration)
        ?.mapNotNull(dataType::createTypeOrNullFrom)
}

/** Configurable object responsible for reading a CSV file. */
public class Reader internal constructor() : Manager() {
    private val csv
        get() = csvReader {
            delimiter = separator.value
            skipEmptyLine = true
        }

    internal operator fun invoke(): List<Map<String, String>>? =
        readResource() ?: readSystemFile()

    private fun readResource(): List<Map<String, String>>? {
        val s: InputStream = (loader getStream "$folder$file") ?: return null
        return csv.readAllWithHeader(s)
    }

    private fun readSystemFile(): List<Map<String, String>>? {
        val url: URL = loader.baseUrl ?: return null
        val f = File("${url.path}$folder$file")
        if (!f.exists()) return null
        return csv.readAllWithHeader(f)
    }

    internal companion object : Factory<Reader>
}
