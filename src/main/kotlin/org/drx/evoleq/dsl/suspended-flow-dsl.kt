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
package org.drx.evoleq.dsl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.SuspendedFlow


class SuspendedFlowConfiguration<D,T> : Configuration<SuspendedFlow<D, T>> {

    var conditions: EvolutionConditions<D, T>? = null
    var flow: (suspend (D)-> Evolving<D>)? = null

    var scope: CoroutineScope = CoroutineScope(Job())

    override fun configure(): SuspendedFlow<D, T> = SuspendedFlow(conditions!!,scope, flow!!)

    fun scope(scope: CoroutineScope) {
        this.scope = scope
    }

    fun conditions(conditions: EvolutionConditions<D, T>) {
        this.conditions = conditions
    }

    fun setupConditions(configuration: EvolutionConditionsConfiguration<D, T>.()->Unit) {
        conditions = org.drx.evoleq.dsl.configure(configuration)
    }

    fun flow(flow: suspend (D)-> Evolving<D>) {
        this.flow = flow
    }
}
fun <D,T> suspendedFlow(configure: SuspendedFlowConfiguration<D,T>.()->Unit) : SuspendedFlow<D, T> = configure(configure)
