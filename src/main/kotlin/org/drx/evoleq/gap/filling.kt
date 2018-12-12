package org.drx.evoleq.gap

import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.evolving.Evolving

open class Filling<P>(private val filler: suspend (P)->Evolving<P>) {
    suspend fun <W> fill(gap: Gap<W,P>): suspend (W)->Evolving<W> = suspended( gap.fill(filler) )
}
