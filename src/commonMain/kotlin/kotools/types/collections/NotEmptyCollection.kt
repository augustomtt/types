package kotools.types.collections

import kotools.types.core.SinceKotoolsTypes
import kotools.types.int.StrictlyPositiveInt

/**
 * Parent of classes representing collections that contain at least one element.
 *
 * @param E The type of elements contained in this collection.
 */
@SinceKotoolsTypes("1.3")
public sealed interface NotEmptyCollection<out E> : Collection<E> {
    /** First element of this collection. */
    public val head: E

    // ---------- Query operations ----------

    /** Returns the [size] of this collection as a [StrictlyPositiveInt]. */
    public val typedSize: StrictlyPositiveInt get() = StrictlyPositiveInt(size)
}
