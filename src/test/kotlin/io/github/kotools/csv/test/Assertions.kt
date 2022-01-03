package io.github.kotools.csv.test

import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal inline fun <T : Any?> assertNotNull(block: () -> T?) {
    assertNotNull(block())
}

internal inline fun <T : Any?> assertNull(block: () -> T?): Unit =
    assertNull(block())

internal infix fun <T : Any> T.assertEquals(other: T): Unit =
    assertEquals(other, this)

internal infix fun <T : Any> T.assertNotEquals(other: T): Unit =
    assertNotEquals(other, this)
