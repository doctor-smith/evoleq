package org.drx.evoleq

interface Spatula<W,P> {
    suspend fun fill(gap: Gap<W,P>): (W)->Evolving<W>
}



