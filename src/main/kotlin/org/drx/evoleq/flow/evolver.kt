package org.drx.evoleq.flow

import org.drx.evoleq.evolving.Evolving

interface Evolver<D> {
    suspend fun evolve(d: D): Evolving<D>
}

