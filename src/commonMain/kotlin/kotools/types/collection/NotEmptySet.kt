package kotools.types.collection

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.internal.ErrorMessage
import kotools.types.internal.ExperimentalSince
import kotools.types.internal.InternalKotoolsTypesApi
import kotools.types.internal.KotoolsTypesVersion
import kotools.types.internal.Since
import kotools.types.internal.serializationError
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmSynthetic

/**
 * Creates a [NotEmptySet] starting with a [head] and containing all the
 * elements of the optional [tail].
 *
 * Here's a simple usage example:
 *
 * ```kotlin
 * val integers: NotEmptySet<Int> = notEmptySetOf(1, 2, 3, 1)
 * println(integers) // [1, 2, 3]
 * ```
 */
@OptIn(InternalKotoolsTypesApi::class)
@Since(KotoolsTypesVersion.V4_0_0)
public fun <E> notEmptySetOf(head: E, vararg tail: E): NotEmptySet<E> {
    val elements: Set<E> = setOf(head) + tail
    return NotEmptySet.orThrow(elements)
}

/**
 * Returns an encapsulated [NotEmptySet] containing all the elements of this
 * collection, or returns an encapsulated [IllegalArgumentException] if this
 * collection is [empty][Collection.isEmpty].
 *
 * Here's a simple usage example:
 *
 * ```kotlin
 * var collection: Collection<Int> = setOf(1, 2, 3, 1)
 * var result: Result<NotEmptySet<Int>> = collection.toNotEmptySet()
 * println(result) // Success([1, 2, 3])
 *
 * collection = emptySet()
 * result = collection.toNotEmptySet()
 * println(result) // Failure(IllegalArgumentException)
 * ```
 *
 * Please note that changes made to the original collection will not be
 * reflected on the resulting [NotEmptySet].
 *
 * ```kotlin
 * val original: MutableCollection<Int> = mutableSetOf(1, 2, 3)
 * val notEmptySet: NotEmptySet<Int> = original.toNotEmptySet()
 *     .getOrThrow()
 * println(original) // [1, 2, 3]
 * println(notEmptySet) // [1, 2, 3]
 *
 * original.clear()
 * println(original) // []
 * println(notEmptySet) // [1, 2, 3]
 * ```
 */
@OptIn(InternalKotoolsTypesApi::class)
@Since(KotoolsTypesVersion.V4_0_0)
public fun <E> Collection<E>.toNotEmptySet(): Result<NotEmptySet<E>> =
    runCatching {
        val elements: Set<E> = toSet()
        NotEmptySet.orThrow(elements)
    }

/**
 * Represents a set that has at least one element of type [E].
 *
 * You can use the [notEmptySetOf] or the [toNotEmptySet] functions for building
 * this type.
 */
@JvmInline
@OptIn(InternalKotoolsTypesApi::class)
@Serializable(NotEmptySetSerializer::class)
@Since(KotoolsTypesVersion.V4_0_0)
public value class NotEmptySet<out E> private constructor(
    private val elements: Set<E>
) : NotEmptyCollection<E> {
    /** Contains static declarations for the [NotEmptySet] type. */
    public companion object {
        /**
         * Creates a [NotEmptySet] containing all the elements of the specified
         * [collection], or returns `null` if the [collection] is
         * [empty][Collection.isEmpty].
         *
         * Here's an example of calling this function from Kotlin code:
         *
         * ```kotlin
         * val collection: Collection<Int> = setOf(1, 2, 3)
         * val elements: NotEmptySet<Int>? =
         *     NotEmptySet.createOrNull(collection)
         * println(elements) // [1, 2, 3]
         * ```
         *
         * The [NotEmptySet] type being an
         * [inline value class](https://kotlinlang.org/docs/inline-classes.html),
         * this function is not available yet for Java users.
         *
         * Please note that changes made to the original collection will not be
         * reflected on the resulting [NotEmptySet].
         *
         * ```kotlin
         * val original: MutableCollection<Int> = mutableSetOf(1, 2, 3)
         * val integers: NotEmptySet<Int>? = NotEmptySet.createOrNull(original)
         * println(original) // [1, 2, 3]
         * println(integers) // [1, 2, 3]
         *
         * original.clear()
         * println(original) // []
         * println(integers) // [1, 2, 3]
         * ```
         */
        @ExperimentalKotoolsTypesApi
        @ExperimentalSince(KotoolsTypesVersion.Unreleased)
        @JvmSynthetic
        public fun <E> createOrNull(
            collection: Collection<E>
        ): NotEmptySet<E>? {
            if (collection.isEmpty()) return null
            val elements: Set<E> = collection.toSet()
            return NotEmptySet(elements)
        }

        @InternalKotoolsTypesApi
        @JvmSynthetic
        internal fun <E> orThrow(elements: Set<E>): NotEmptySet<E> {
            val isValid: Boolean = elements.isNotEmpty()
            require(isValid) { ErrorMessage.emptyCollection }
            return NotEmptySet(elements)
        }
    }

    override val head: E get() = elements.first()

    override val tail: NotEmptySet<E>?
        get() = elements.drop(1)
            .toNotEmptySet()
            .getOrNull()

    /**
     * Returns all elements of this set as a [Set] of type [E].
     *
     * Here's a simple usage example:
     *
     * ```kotlin
     * val notEmptySet: NotEmptySet<Int> = notEmptySetOf(1, 2, 3, 1)
     * val set: Set<Int> = notEmptySet.toSet()
     * println(set) // [1, 2, 3]
     * ```
     */
    public fun toSet(): Set<E> = elements

    override fun toString(): String = "$elements"
}

@InternalKotoolsTypesApi
internal class NotEmptySetSerializer<E>(elementSerializer: KSerializer<E>) :
    KSerializer<NotEmptySet<E>> {
    private val delegate: KSerializer<Set<E>> by lazy {
        SetSerializer(elementSerializer)
    }

    override val descriptor: SerialDescriptor by lazy(delegate::descriptor)

    override fun serialize(encoder: Encoder, value: NotEmptySet<E>) {
        val elements: Set<E> = value.toSet()
        encoder.encodeSerializableValue(delegate, elements)
    }

    override fun deserialize(decoder: Decoder): NotEmptySet<E> = decoder
        .decodeSerializableValue(delegate)
        .toNotEmptySet()
        .getOrElse { serializationError(ErrorMessage.emptyCollection) }
}
