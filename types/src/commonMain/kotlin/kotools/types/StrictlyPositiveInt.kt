package kotools.types

import kotools.shared.Project.Types
import kotools.shared.SinceKotools
import kotools.shared.StabilityLevel
import kotools.types.number.aStrictlyPositiveNumber
import kotools.types.number.shouldBe
import kotlin.jvm.JvmInline

/** Representation of strictly positive integers, excluding zero. */
@JvmInline
@SinceKotools(Types, "3.2", StabilityLevel.Alpha)
public value class StrictlyPositiveInt
private constructor(private val value: Int) : Comparable<StrictlyPositiveInt>,
    ExplicitInt {
    internal companion object {
        infix fun of(value: Int): Result<StrictlyPositiveInt> = value
            .takeIf { it > 0 }
            ?.let(::StrictlyPositiveInt)
            ?.let(Result.Companion::success)
            ?: Result.failure(value shouldBe aStrictlyPositiveNumber)
    }

    /**
     * Compares this integer with the [other] one for order.
     * Returns zero if this integer equals the [other] one, a negative number if
     * this integer is less than the [other] one, or a positive number if
     * this integer is greater than the [other] one.
     */
    override fun compareTo(other: StrictlyPositiveInt): Int =
        value.compareTo(other.value)

    internal fun toPositiveInt(): PositiveInt = value.toPositiveInt()
        .getOrThrow()

    override fun toInt(): Int = value
}

/**
 * Returns this integer as a [StrictlyPositiveInt], or an
 * [IllegalArgumentException] if this integer is negative.
 */
@SinceKotools(Types, "3.2", StabilityLevel.Alpha)
public fun Int.toStrictlyPositiveInt(): Result<StrictlyPositiveInt> =
    StrictlyPositiveInt of this
