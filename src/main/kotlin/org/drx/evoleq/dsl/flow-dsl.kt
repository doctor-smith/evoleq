/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.flow.SuspendedFlow

class FlowConfiguration<D,T> : Configuration<Flow<D, T>> {

    var conditions: EvolutionConditions<D, T>? = null
    var flow: ((D)-> Evolving<D>)? = null

    override fun configure(): Flow<D, T> = Flow(conditions!!, flow!!)

    fun conditions(conditions: EvolutionConditions<D, T>) {
        this.conditions = conditions
    }

    fun flow(flow:(D)-> Evolving<D>) {
        this.flow = flow
    }
}
fun <D,T> flow(configure: FlowConfiguration<D,T>.()->Unit) : Flow<D, T> = org.drx.evoleq.dsl.configure(configure)
