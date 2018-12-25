/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.math

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.flow.SuspendedFlow
import org.drx.evoleq.flow.fill
import org.drx.evoleq.gap.Gap

/**
 * Compose flows
 */
fun <D,S,E,T> Flow<D, S>.then(
    phi: Flow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (Flow<E,T>)-> Flow<D,T> = {flow: Flow<E,T> -> gap.fill(flow)}
    val evolveThis: suspend (D)-> Evolving<D> = { d:D -> this@then.evolve(d) }
    val evolvePhi: suspend (D)-> Evolving<D> = { d:D -> filler(phi).evolve(d) }
    val combined: suspend (D)->Evolving<D> = {d: D -> (evolveThis * evolvePhi)(d)}
    flow = {d ->combined(d)}
}

/**
 * compose suspended flow with flow
 */
fun <D,S,E,T> SuspendedFlow<D, S>.then(
    phi: Flow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (Flow<E,T>)-> Flow<D,T> = {flow: Flow<E,T> -> gap.fill(flow)}
    val evolvePhi: suspend (D)-> Evolving<D> = { d:D -> filler(phi).evolve(d) }
    val combined: suspend (D)->Evolving<D> = {d: D -> (this@then.flow * evolvePhi)(d)}
    flow = {d ->combined(d)}
}

/**
 * Compose flow with suspended flow
 */
fun <D,S,E,T> Flow<D, S>.then(
    phi: SuspendedFlow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (SuspendedFlow<E,T>)-> SuspendedFlow<D,T> = {flow: SuspendedFlow<E,T> -> gap.fill(flow)}
    val evolveThis: suspend (D)-> Evolving<D> = { d:D -> this@then.evolve(d) }
    val combined: suspend (D)->Evolving<D> = {d: D -> (evolveThis * filler(phi).flow)(d)}
    flow = {d ->combined(d)}
}

/**
 * Compose suspended flows
 */
fun <D,S,E,T> SuspendedFlow<D, S>.then(
    phi: SuspendedFlow<E,T>,
    cond: EvolutionConditions<D,T>,
    gap: Gap<D, E>
): SuspendedFlow<D, T> = suspendedFlow{
    conditions = cond
    val filler: suspend (SuspendedFlow<E,T>)-> SuspendedFlow<D,T> = {flow: SuspendedFlow<E,T> -> gap.fill(flow)}
    val combined: suspend (D)->Evolving<D> = {d: D -> (this@then.flow * filler(phi).flow)(d)}
    flow = {d ->combined(d)}
}

