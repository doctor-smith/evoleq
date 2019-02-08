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