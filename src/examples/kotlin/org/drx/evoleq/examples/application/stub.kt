package org.drx.evoleq.examples.application

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate

interface Stub<D> {
    suspend fun stub(d: D): Evolving<D>
}
class InitStub<D> : Stub<D>{
    override suspend fun stub(d: D): Evolving<D> {
        return Immediate{d}
    }
}