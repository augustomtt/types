package kotools.types.collections

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.shared.Project.Types
import kotools.shared.SinceKotools
import kotools.shared.StabilityLevel
import kotools.types.Package

// ---------- Builders ----------

/**
 * Creates a [NotEmptyList] starting with a [head] and containing all the
 * elements of the optional [tail].
 */
@Deprecated(
    "Use the notEmptyListOf function instead.",
    ReplaceWith(
        "notEmptyListOf<E>(head, *tail)",
        "${Package.collections}.notEmptyListOf"
    ),
    DeprecationLevel.ERROR
)
@SinceKotools(Types, "1.3")
@Suppress("FunctionName")
public fun <E> NotEmptyList(head: E, vararg tail: E): NotEmptyList<E> {
    val list: List<E> = tail.toList()
    return NotEmptyList(head, list)
}

@Suppress("FunctionName")
private fun <E> NotEmptyList(head: E, tail: List<E>): NotEmptyList<E> {
    val list: List<E> = listOf(head) + tail
    return NotEmptyListImplementation(list)
}

/**
 * Creates a [NotEmptyList] starting with a [head] and containing all the
 * elements of the optional [tail].
 */
@SinceKotools(Types, "3.0")
public fun <E> notEmptyListOf(head: E, vararg tail: E): NotEmptyList<E> {
    val list: List<E> = tail.toList()
    return notEmptyListOf(head, list)
}

private fun <E> notEmptyListOf(head: E, tail: List<E>): NotEmptyList<E> {
    val list: List<E> = listOf(head) + tail
    return NotEmptyListImplementation(list)
}

/**
 * Returns a [NotEmptyList] containing all the elements of this array, or throws
 * an [IllegalArgumentException] if this array is empty.
 */
@Deprecated(
    "Use the Array.toNotEmptyListOrThrow function instead.",
    ReplaceWith(
        "this.toNotEmptyListOrThrow()",
        "${Package.collections}.toNotEmptyListOrThrow"
    )
)
@SinceKotools(Types, "1.3")
@Throws(IllegalArgumentException::class)
public fun <E> Array<E>.toNotEmptyList(): NotEmptyList<E> =
    toNotEmptyListOrThrow()

/**
 * Returns a [NotEmptyList] containing all the elements of this collection, or
 * throws an [IllegalArgumentException] if this collection is empty.
 */
@Deprecated(
    "Use the Collection.toNotEmptyListOrThrow function instead.",
    ReplaceWith(
        "this.toNotEmptyListOrThrow()",
        "${Package.collections}.toNotEmptyListOrThrow"
    )
)
@SinceKotools(Types, "1.3")
@Throws(IllegalArgumentException::class)
public fun <E> Collection<E>.toNotEmptyList(): NotEmptyList<E> =
    toNotEmptyListOrThrow()

/**
 * Returns a [NotEmptyList] containing all the elements of this array, or
 * returns the result of calling the [defaultValue] function if this array is
 * empty.
 */
@SinceKotools(Types, "1.3")
public inline infix fun <E> Array<E>.toNotEmptyListOrElse(
    defaultValue: (Array<E>) -> NotEmptyList<E>
): NotEmptyList<E> = toNotEmptyListOrNull() ?: defaultValue(this)

/**
 * Returns a [NotEmptyList] containing all the elements of this collection, or
 * returns the result of calling the [defaultValue] function if this collection
 * is empty.
 */
@SinceKotools(Types, "1.3")
public inline infix fun <E> Collection<E>.toNotEmptyListOrElse(
    defaultValue: (Collection<E>) -> NotEmptyList<E>
): NotEmptyList<E> = toNotEmptyListOrNull() ?: defaultValue(this)

/**
 * Returns a [NotEmptyList] containing all the elements of this array, or
 * returns `null` if this array is empty.
 */
@SinceKotools(Types, "1.3")
public fun <E> Array<E>.toNotEmptyListOrNull(): NotEmptyList<E>? = toList()
    .toNotEmptyListOrNull()

/**
 * Returns a [NotEmptyList] containing all the elements of this collection, or
 * returns `null` if this collection is empty.
 */
@SinceKotools(Types, "1.3")
public fun <E> Collection<E>.toNotEmptyListOrNull(): NotEmptyList<E>? =
    takeIf(Collection<E>::isNotEmpty)
        ?.toList()
        ?.run { first() to subList(1, size) }
        ?.run { notEmptyListOf(first, second) }

/**
 * Returns a [NotEmptyList] containing all the elements of this array, or throws
 * an [IllegalArgumentException] if this array is empty.
 */
@SinceKotools(Types, "3.2", StabilityLevel.Alpha)
@Throws(IllegalArgumentException::class)
public fun <E> Array<E>.toNotEmptyListOrThrow(): NotEmptyList<E> =
    toNotEmptyListOrNull()
        ?: throw IllegalArgumentException("Given array shouldn't be empty.")

/**
 * Returns a [NotEmptyList] containing all the elements of this collection, or
 * throws an [IllegalArgumentException] if this collection is empty.
 */
@SinceKotools(Types, "3.2", StabilityLevel.Alpha)
@Throws(IllegalArgumentException::class)
public fun <E> Collection<E>.toNotEmptyListOrThrow(): NotEmptyList<E> =
    toNotEmptyListOrNull() ?: throw IllegalArgumentException(
        "Given collection shouldn't be empty."
    )

/**
 * Representation of lists that contain at least one element.
 *
 * @param E The type of elements contained in this list.
 */
@Serializable(NotEmptyListSerializer::class)
@SinceKotools(Types, "1.3")
public sealed interface NotEmptyList<out E> : NotEmptyCollection<E>, List<E>

private class NotEmptyListImplementation<out E>(private val list: List<E>) :
    NotEmptyList<E>,
    List<E> by list {
    override val head: E get() = list.first()

    // ---------- Conversions ----------

    override fun toString(): String = list.toString()
}

internal class NotEmptyListSerializer<E>(elementSerializer: KSerializer<E>) :
    KSerializer<NotEmptyList<E>> {
    private val delegate: KSerializer<List<E>> =
        ListSerializer(elementSerializer)

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: NotEmptyList<E>): Unit =
        delegate.serialize(encoder, value)

    override fun deserialize(decoder: Decoder): NotEmptyList<E> = delegate
        .deserialize(decoder)
        .toNotEmptyListOrThrow()
}
