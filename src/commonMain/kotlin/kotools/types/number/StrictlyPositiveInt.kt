package kotools.types.number

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotools.types.ExperimentalSinceKotoolsTypes
import kotools.types.Package
import kotools.types.SinceKotoolsTypes
import kotools.types.experimental.ExperimentalNumberApi
import kotools.types.experimental.ExperimentalRangeApi
import kotools.types.range.NotEmptyRange
import kotools.types.range.notEmptyRangeOf
import kotools.types.text.NotBlankString
import kotools.types.text.toNotBlankString
import kotlin.jvm.JvmInline

/**
 * Returns this number as an encapsulated [StrictlyPositiveInt], which may
 * involve rounding or truncation, or returns an encapsulated
 * [IllegalArgumentException] if this number is [negative][NegativeInt].
 */
@SinceKotoolsTypes("4.1")
public fun Number.toStrictlyPositiveInt(): Result<StrictlyPositiveInt> =
    runCatching {
        val value: Int = toInt()
        StrictlyPositiveInt(value)
    }

/**
 * Returns this number as a [StrictlyPositiveInt], which may involve rounding
 * or truncation, or returns the result of calling the [onFailure] function if
 * this number is negative.
 *
 * Here's some usage examples:
 *
 * ```kotlin
 * var result: StrictlyPositiveInt = 1.toStrictlyPositiveIntOrElse { throw it }
 * println(result) // 1
 *
 * 0.toStrictlyPositiveIntOrElse { throw it } // IllegalArgumentException
 *
 * result = (-1).toStrictlyPositiveIntOrElse {
 *     3.toStrictlyPositiveIntOrThrow()
 * }
 * println(result) // 3
 * ```
 *
 * Instead of returning the result of calling the [onFailure] function when this
 * number is negative, you can use the [toStrictlyPositiveIntOrNull] for
 * returning `null`, or the [toStrictlyPositiveIntOrThrow] for throwing an
 * [IllegalArgumentException].
 */
@ExperimentalNumberApi
@ExperimentalSinceKotoolsTypes("4.3")
public inline fun Number.toStrictlyPositiveIntOrElse(
    onFailure: (IllegalArgumentException) -> StrictlyPositiveInt
): StrictlyPositiveInt = TODO()

/**
 * Returns this number as a [StrictlyPositiveInt], which may involve rounding
 * or truncation, or returns `null` if this number is negative.
 *
 * Here's some usage examples:
 *
 * ```kotlin
 * var result: StrictlyPositiveInt? = 1.toStrictlyPositiveIntOrNull()
 * println(result) // 1
 *
 * result = 0.toStrictlyPositiveIntOrNull()
 * println(result) // null
 *
 * result = (-1).toStrictlyPositiveIntOrNull()
 * println(result) // null
 * ```
 *
 * Instead of returning `null` when this number is negative, you can use the
 * [toStrictlyPositiveIntOrElse] for returning the result of calling the
 * specified callback, or the [toStrictlyPositiveIntOrThrow] for throwing an
 * [IllegalArgumentException].
 */
@ExperimentalNumberApi
@ExperimentalSinceKotoolsTypes("4.3")
public fun Number.toStrictlyPositiveIntOrNull(): StrictlyPositiveInt? = TODO()

/**
 * Returns this number as a [StrictlyPositiveInt], which may involve rounding
 * or truncation, or throws [IllegalArgumentException] if this number is
 * negative.
 *
 * Here's some usage examples:
 *
 * ```kotlin
 * val result: StrictlyPositiveInt = 1.toStrictlyPositiveIntOrThrow()
 * println(result) // 1
 *
 * 0.toStrictlyPositiveIntOrThrow() // IllegalArgumentException
 * (-1).toStrictlyPositiveIntOrThrow() // IllegalArgumentException
 * ```
 *
 * Instead of throwing an [IllegalArgumentException] when this number is
 * negative, you can use the [toStrictlyPositiveIntOrElse] for returning the
 * result of calling the specified callback, or the
 * [toStrictlyPositiveIntOrNull] for returning `null`.
 */
@ExperimentalNumberApi
@ExperimentalSinceKotoolsTypes("4.3")
public fun Number.toStrictlyPositiveIntOrThrow(): StrictlyPositiveInt = TODO()

/** Representation of positive integers excluding [zero][ZeroInt]. */
@JvmInline
@Serializable(StrictlyPositiveIntSerializer::class)
@SinceKotoolsTypes("1.1")
public value class StrictlyPositiveInt
internal constructor(private val value: Int) : NonZeroInt, PositiveInt {
    init {
        require(value > 0) {
            "Number should be strictly positive (tried with $this)."
        }
    }

    @SinceKotoolsTypes("4.0")
    override fun toInt(): Int = value

    @SinceKotoolsTypes("4.0")
    override fun toString(): String = "$value"

    /**
     * Contains declarations for holding or building a [StrictlyPositiveInt].
     */
    public companion object {
        /** The minimum value a [StrictlyPositiveInt] can have. */
        public val min: StrictlyPositiveInt by lazy(
            1.toStrictlyPositiveInt()::getOrThrow
        )

        /** The maximum value a [StrictlyPositiveInt] can have. */
        public val max: StrictlyPositiveInt by lazy(
            Int.MAX_VALUE.toStrictlyPositiveInt()::getOrThrow
        )

        /** The range of values a [StrictlyPositiveInt] can have. */
        @ExperimentalRangeApi
        @ExperimentalSinceKotoolsTypes("4.2")
        public val range: NotEmptyRange<StrictlyPositiveInt> by lazy {
            val start: StrictlyPositiveInt = 1
                .toStrictlyPositiveInt()
                .getOrThrow()
            val end: StrictlyPositiveInt = Int.MAX_VALUE
                .toStrictlyPositiveInt()
                .getOrThrow()
            notEmptyRangeOf { start.inclusive to end.inclusive }
        }

        /** Returns a random [StrictlyPositiveInt]. */
        @SinceKotoolsTypes("3.0")
        public fun random(): StrictlyPositiveInt = (min.value..max.value)
            .random()
            .toStrictlyPositiveInt()
            .getOrThrow()
    }
}

/** Returns the negative of this integer. */
@ExperimentalNumberApi
@ExperimentalSinceKotoolsTypes("4.2")
public operator fun StrictlyPositiveInt.unaryMinus(): StrictlyNegativeInt =
    toInt()
        .unaryMinus()
        .toStrictlyNegativeInt()
        .getOrThrow()

internal object StrictlyPositiveIntSerializer :
    AnyIntSerializer<StrictlyPositiveInt> {
    override val serialName: Result<NotBlankString> by lazy(
        "${Package.number}.StrictlyPositiveInt"::toNotBlankString
    )

    override fun deserialize(value: Int): StrictlyPositiveInt = value
        .toStrictlyPositiveInt()
        .getOrNull()
        ?: throw SerializationException(value shouldBe aStrictlyPositiveNumber)
}
