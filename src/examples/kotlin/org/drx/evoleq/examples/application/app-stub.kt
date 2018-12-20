package org.drx.evoleq.examples.application

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import kotlin.reflect.KClass

interface ApplicationStub<D>: Stub<D>

class InitAppStub<D> : ApplicationStub<D> {

    val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
    override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

    override suspend fun stub(d: D): Evolving<D> {
        return Immediate{d}
    }
}