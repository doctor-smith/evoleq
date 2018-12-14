package org.drx.evoleq.dsl

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.flow.SuspendedFlow

class FlowConfiguration<D,T> : Configuration<Flow<D, T>> {

    var conditions: EvolutionConditions<D, T>? = null
    var flow: ((D)-> Evolving<D>)? = null

    override fun configure(): Flow<D, T> = Flow(conditions!!, flow!!)
}
fun <D,T> flow(configure: FlowConfiguration<D,T>.()->Unit) : Flow<D, T> = org.drx.evoleq.dsl.configure(configure)
