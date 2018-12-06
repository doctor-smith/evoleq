package org.drx.evoleq.flow

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*
import org.drx.evoleq.data.Evolving
import org.drx.evoleq.data.Immediate
import org.drx.evoleq.data.Parallel
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.data.times

interface Evolver<D> {
    suspend fun evolve(d: D): Evolving<D>
}
data class Stubby<D,C>(val data: D, val iota:(C)-> Evolving<D>, val stub:(C)-> Evolving<C>)



open class Flow<D, T>(
    val conditions: EvolutionConditions<D, T>,
    val flow: (D)-> Evolving<D>
) : Evolver<D> {
    override suspend fun evolve(data: D): Evolving<D> =
        Immediate {
            org.drx.evoleq.evolve(
                initialData = data,
                conditions = conditions
            ) { data ->
                flow(data)
            }
        }
}


open class StubbyFlow<D,S,E,T>(

    val mainFlow: Flow<D, S>,
    val stubbyFlow: Flow<E, T>,
    val stubby: Stubby<D, E>
) : Flow<D, S>( mainFlow.conditions, mainFlow.flow) {

}

suspend fun <D,T,P> Flow<D, T>.enter(gap: Gap<D, P>): Gap<D, P> =
    Gap({ d -> Immediate { (this.flow * gap.from)(d).get() } }, gap.to)

suspend fun <D,T,P> Gap<D, P>.fill(phi: Flow<P, T>, conditions: EvolutionConditions<D, T>): Flow<D, T> =
    Flow(conditions) { data ->
        Immediate { this@fill.fill(phi.flow)(data).get() }
    }
suspend fun <D,T,P> Gap<D, P>.fill(phi: Flow<P, T>): Flow<D, T> =
    Flow(this@fill.adapt(phi.conditions)) { data ->
        Immediate { this@fill.fill(phi.flow)(data).get() }
    }

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

suspend fun <D,T,P> Gap<D, P>.fillParallel(phi: Flow<P, T>, conditions: EvolutionConditions<D, T>): Flow<D, T> =
    Flow(conditions) { data ->
        Parallel { this@fillParallel.fill(phi.flow)(data).get() }
    }



