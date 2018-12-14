package org.drx.evoleq.dsl

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.SuspendedFlow


class SuspendedFlowConfiguration<D,T> : Configuration<SuspendedFlow<D, T>> {

    var conditions: EvolutionConditions<D, T>? = null
    var flow: (suspend (D)-> Evolving<D>)? = null

    override fun configure(): SuspendedFlow<D, T> = SuspendedFlow(conditions!!, flow!!)
}
fun <D,T> suspendedFlow(configure: SuspendedFlowConfiguration<D,T>.()->Unit) : SuspendedFlow<D, T> = configure(configure)
