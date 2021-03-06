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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.drx.evoleq.annotation.Experimental
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.evolveSuspended
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.LazyEvolving
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.math.map
import org.drx.evoleq.math.times
import org.drx.evoleq.stub.DefaultIdentificationKey
import org.drx.evoleq.stub.Stub
import kotlin.reflect.KClass

/**
 * Base class for suspended flows,
 * Standard implementation of Evolver
 */
open class SuspendedFlow<D, T>(

    val conditions: EvolutionConditions<D, T>,
    override val scope: CoroutineScope = CoroutineScope(Job()),
    val flow: suspend (D)-> Evolving<D>
) : Evolver<D> {
    override suspend fun evolve(d: D): Evolving<D> =
        scope.parallel {
            evolveSuspended(
                initialData = d,
                conditions = conditions,
                scope = this
            ) {
                data -> flow(data)
            }
        }.map(scope){parallel -> parallel.get()}

}
@Experimental
@Suppress("FunctionName")
fun <D,T> CoroutineScope.LazyFlow(
    conditions: EvolutionConditions<D, T>,
    flow: LazyEvolving<D>
): Evolver<D> = object: LazyEvolver<D> {
    override val scope: CoroutineScope
        get() = this@LazyFlow

    override suspend fun lazy(): LazyEvolving<D> = { d: D ->
            evolveSuspended(
                d,
                conditions,
                this
            ) {
                data -> flow(data)
            }
    }
}

/**
 * Suspended flow enters gap from the left
 */
suspend fun <D,T,P> SuspendedFlow<D, T>.enter(gap: Gap<D, P>): Gap<D, P> =
    Gap(
        from = { d -> scope.parallel  { (flow * gap.from)(d).get() } },
        to = gap.to
    )

/**
 * Fill gap with a suspended flow
 */
suspend fun <D,T,P> Gap<D, P>.fill(phi: SuspendedFlow<P, T>, conditions: EvolutionConditions<D, T>): SuspendedFlow<D, T> =
    SuspendedFlow(
        conditions = conditions
    ) {
            data -> phi.scope.parallel  { this@fill.fill(phi.flow)(data).get() }
    }

/**
 * Fill gap with a suspended  flow
 */
suspend fun <D,T,P> Gap<D, P>.fill(phi: SuspendedFlow<P, T>): SuspendedFlow<D, T> =
    SuspendedFlow(
        conditions = this@fill.adapt(phi.conditions)
    ){
            data -> phi.scope.parallel  { this@fill.fill(phi.flow)(data).get() }
    }

/**
 * Fill gap with a suspended flow in a parallel manner
 */
suspend fun <D,T,P> Gap<D, P>.fillParallel(phi: SuspendedFlow<P, T>, conditions: EvolutionConditions<D, T>): Flow<D, T> =
    Flow(
        conditions = conditions
    ) {
            data -> phi.scope.parallel { this@fillParallel.fill(phi.flow)(data).get() }
    }

fun <D,T> SuspendedFlow<D,T>.toStub(id: KClass<*> = DefaultIdentificationKey::class): Stub<D> = stub<D>{
    id(id)
    evolve{ data -> this@toStub.evolve(data) }
}