package org.drx.evoleq.experimental.app

import org.drx.evoleq.evolving.Evolving

interface ApplicationStub<D> {
    suspend fun stub(d: D): Evolving<D>
}