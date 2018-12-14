package org.drx.evoleq.examples.application

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate

interface ApplicationStub<D>: Stub<D>

class InitAppStub<D> : ApplicationStub<D> {
    override suspend fun stub(d: D): Evolving<D> {
        return Immediate{d}
    }
}