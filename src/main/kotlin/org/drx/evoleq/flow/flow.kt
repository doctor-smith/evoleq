/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.flow

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.evolve
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.math.times
import org.drx.evoleq.stub.DefaultIdentificationKey
import org.drx.evoleq.stub.Stub
import kotlin.reflect.KClass

/**
 * Base class for flows,
 * Standard implementation of Evolver
 */
open class Flow<D, T>(

    val conditions: EvolutionConditions<D, T>,
    override val scope: CoroutineScope = CoroutineScope(Job()),
    val flow: (D)-> Evolving<D>
) : Evolver<D> {
    override suspend fun evolve(d: D): Evolving<D> =
            evolve(
                initialData = d,
                conditions = conditions
            ) {
                    data -> flow(data)
            }
}

/**
 * Flow enters gap from the left
 */
suspend fun <D,T,P> Flow<D, T>.enter(gap: Gap<D, P>): Gap<D, P> =
    Gap(
        from = { d -> Parallel { (flow * gap.from)(d).get() } },
        to = gap.to
    )


/**
 * Fill gap with a flow
 */
suspend fun <D,T,P> Gap<D, P>.fill(phi: Flow<P, T>, conditions: EvolutionConditions<D, T>): Flow<D, T> =
    Flow(
        conditions = conditions
    ) {
            data -> Parallel { this@fill.fill(phi.flow)(data).get() }
    }

/**
 * Fill gap with a flow
 */
suspend fun <D,T,P> Gap<D, P>.fill(phi: Flow<P, T>): Flow<D, T> =
    Flow(
        conditions = this@fill.adapt(phi.conditions)
    ){
            data -> Parallel { this@fill.fill(phi.flow)(data).get() }
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


fun <D,T> Flow<D,T>.toStub(id: KClass<*> = DefaultIdentificationKey::class): Stub<D> = stub<D>{
    id(id)
    evolve{ data -> this@toStub.evolve(data) }
}
