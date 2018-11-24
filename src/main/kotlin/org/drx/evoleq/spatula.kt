package org.drx.evoleq

interface Spatula<W,P> {
    suspend fun fill(gap: Gap<W,P>): (W)->Evolving<W>
}

data class Gap<W,P>(
    val from:(W)->Evolving<P>,
    val to:(P)->Evolving<W>
)


