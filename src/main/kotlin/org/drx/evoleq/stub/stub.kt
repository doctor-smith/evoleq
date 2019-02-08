package org.drx.evoleq.stub

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.flow.Evolver
import org.drx.evoleq.flow.SuspendedFlow
import kotlin.reflect.KClass

interface Stub<D> : Evolver<D> {
    override suspend fun evolve(d: D): Evolving<D> = Immediate{ d }

    val stubs: HashMap<KClass<*>, Stub<*>>
}
class ParentStubKey
fun <D,T> Stub<D>.toFlow(conditions: EvolutionConditions<D,T>): SuspendedFlow<D,T> = suspendedFlow {
    conditions(conditions)
    flow{ d -> this@toFlow.evolve(d) }
}

open class InitStub<D> : Stub<D>{
    override val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
}