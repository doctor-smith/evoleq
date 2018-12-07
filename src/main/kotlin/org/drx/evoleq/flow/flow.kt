package org.drx.evoleq.flow

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolve
import org.drx.evoleq.evolveSuspended
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.math.times

/**
 * Base class for flows,
 * Standard implementation of Evolver
 */
open class Flow<D, T>(
    val conditions: EvolutionConditions<D, T>,
    val flow: (D)-> Evolving<D>
) : Evolver<D> {
    override suspend fun evolve(data: D): Evolving<D> =
        Immediate {
            evolve(
                initialData = data,
                conditions = conditions
            ) {
                    data -> flow(data)
            }
        }
}

/**
 * Flow enters gap from the left
 */
suspend fun <D,T,P> Flow<D, T>.enter(gap: Gap<D, P>): Gap<D, P> =
    Gap(
        from = { d -> Immediate { (this.flow * gap.from)(d).get() } },
        to = gap.to
    )


/**
 * Fill gap with a flow
 */
suspend fun <D,T,P> Gap<D, P>.fill(phi: Flow<P, T>, conditions: EvolutionConditions<D, T>): Flow<D, T> =
    Flow(
        conditions = conditions
    ) {
            data -> Immediate { this@fill.fill(phi.flow)(data).get() }
    }

/**
 * Fill gap with a flow
 */
suspend fun <D,T,P> Gap<D, P>.fill(phi: Flow<P, T>): Flow<D, T> =
    Flow(
        conditions = this@fill.adapt(phi.conditions)
    ){
            data -> Immediate { this@fill.fill(phi.flow)(data).get() }
    }



/**
 * Adapt evolution conditions in a gap
 */
suspend fun <D,T,P> Gap<D, P>.adapt(conditions: EvolutionConditions<P, T>): EvolutionConditions<D, T> {
    val prop = SimpleObjectProperty<T>()
    fun update(data: D): T  {
        var unset = true
        GlobalScope.launch{
            prop.value = async{
                conditions.updateCondition(this@adapt.from(data).get())
            }.await()
            unset = false
        }
        while(unset){
            Thread.sleep(0,1_000)
        }
        unset = true
        return prop.value
    }
    return EvolutionConditions(
        testObject = conditions.testObject,
        check = conditions.check,
        updateCondition = { data -> update(data) }
    )
}

/**
 * Fill gap with a flow in a parallel manner
 */
suspend fun <D,T,P> Gap<D, P>.fillParallel(phi: Flow<P, T>, conditions: EvolutionConditions<D, T>): Flow<D, T> =
    Flow(
        conditions = conditions
    ) {
            data -> Parallel { this@fillParallel.fill(phi.flow)(data).get() }
    }



