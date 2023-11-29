/*
 * Copyright 2023 Kotools S.A.S.U.
 * Use of this source code is governed by the MIT license.
 */

package kotools.types.experimental

import kotools.types.internal.ExperimentalSince
import kotools.types.internal.KotoolsTypesVersion
import kotools.types.internal.unexpectedCreationError
import kotools.types.number.NegativeInt
import kotools.types.number.PositiveInt
import kotools.types.number.toNegativeInt
import kotlin.jvm.JvmSynthetic

/**
 * Returns the negative of this integer number.
 *
 * Here's an example of calling this function from Kotlin code:
 *
 * ```kotlin
 * val number: PositiveInt = 1.toPositiveInt()
 *     .getOrThrow()
 * val result: NegativeInt = -number // or number.unaryMinus()
 * println(result) // -1
 * ```
 *
 * Please note that this function is currently not available for Java users.
 */
@ExperimentalKotoolsTypesApi
@ExperimentalSince(KotoolsTypesVersion.V4_3_3)
@JvmSynthetic
public operator fun PositiveInt.unaryMinus(): NegativeInt {
    val value: Int = toInt()
        .unaryMinus()
    return value.toNegativeInt()
        .getOrNull()
        ?: unexpectedCreationError<NegativeInt>(value)
}
