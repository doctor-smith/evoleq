package org.drx.evoleq.dsl

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.SuspendedFlow


class SuspendedFlowConfiguration<D,T>(
    var conditions: EvolutionConditions<D, T>?,
    var flow: (suspend (D)-> Evolving<D>)?
) : Configuration<SuspendedFlow<D, T>> {
    override fun configure(): SuspendedFlow<D, T> = SuspendedFlow(conditions!!, flow!!)
}
fun <D,T> suspendedFlow(configure: SuspendedFlowConfiguration<D,T>.()->Unit) : SuspendedFlow<D, T> = org.drx.evoleq.dsl.configure(configure)
