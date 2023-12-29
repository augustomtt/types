/*
 * Copyright 2023 Kotools S.A.S.
 * Use of this source code is governed by the MIT license.
 */

@file:Suppress("unused")

package sample.kotools.types.experimental

import kotools.types.experimental.ExperimentalKotoolsTypesApi
import kotools.types.experimental.positiveRange
import kotools.types.experimental.unaryMinus
import kotools.types.number.NonZeroInt
import kotools.types.number.toNonZeroInt

@ExperimentalKotoolsTypesApi
private fun nonZeroInt_unaryMinus_Kotlin() {
    val number: NonZeroInt = 1.toNonZeroInt().getOrThrow()
    val result: NonZeroInt = -number // or number.unaryMinus()
    println(result) // -1
}

@ExperimentalKotoolsTypesApi
private fun nonZeroInt_Companion_positiveRange() {
    println(NonZeroInt.positiveRange) // [1;2147483647]
}
