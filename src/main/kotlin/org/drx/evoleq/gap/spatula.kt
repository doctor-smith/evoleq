package org.drx.evoleq.gap

import org.drx.evoleq.data.Evolving

interface Spatula<W,P> {
    suspend fun fill(gap: Gap<W, P>): (W)-> Evolving<W>
}



