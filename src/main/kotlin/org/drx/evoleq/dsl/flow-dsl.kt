package org.drx.evoleq.dsl

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.flow.SuspendedFlow

class FlowConfiguration<D,T>(
    var conditions: EvolutionConditions<D, T>?,
    var flow: ((D)-> Evolving<D>)?
) : Configuration<Flow<D, T>> {
    override fun configure(): Flow<D, T> = Flow(conditions!!, flow!!)
}
fun <D,T> flow(configure: FlowConfiguration<D,T>.()->Unit) : Flow<D, T> = org.drx.evoleq.dsl.configure(configure)


class SuspendedFlowConfiguration<D,T>(
    var conditions: EvolutionConditions<D, T>?,
    var flow: (suspend (D)-> Evolving<D>)?
) : Configuration<SuspendedFlow<D, T>> {
    override fun configure(): SuspendedFlow<D, T> = SuspendedFlow(conditions!!, flow!!)
}
fun <D,T> suspendedFlow(configure: SuspendedFlowConfiguration<D,T>.()->Unit) : SuspendedFlow<D, T> = org.drx.evoleq.dsl.configure(configure)
