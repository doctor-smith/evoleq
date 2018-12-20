package org.drx.evoleq.examples.application

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import kotlin.reflect.KClass

interface Stub<D> {
    suspend fun stub(d: D): Evolving<D>
    fun stubs(): HashMap<KClass<*>, Stub<*>>
}
class InitStub<D> : Stub<D>{
    override suspend fun stub(d: D): Evolving<D> {
        return Immediate{d}
    }
    val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
    override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs
}