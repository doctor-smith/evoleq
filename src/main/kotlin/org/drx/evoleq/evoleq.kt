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
package org.drx.evoleq

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.conditions.ok
import org.drx.evoleq.conditions.update
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.math.etaEvolving

/**
 * A cancellable scope enforcing structured concurrency
 * To be used as default by all implementations of the [Evolving] type
 */
@Suppress("FunctionName")
fun DefaultEvolutionScope(): CoroutineScope =  CoroutineScope(Job())

/**
 * Evolution
 */
tailrec suspend fun <D, T> evolve(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    scope: CoroutineScope = DefaultEvolutionScope(),
    flow: (D) -> Evolving<D>
) : Evolving<D> = when( conditions.ok() ) {
    false -> etaEvolving( initialData )
    true -> {
        val evolvedData: D = flow ( initialData ).get()
        evolve(
            evolvedData,
            conditions.update( evolvedData ),
            scope
        ){
            data -> flow ( data )
        }
    }
}

/**
 * Evolution
 */
tailrec suspend fun <D, T> evolveSuspended(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    scope: CoroutineScope = DefaultEvolutionScope(),
    flow: suspend CoroutineScope.(D) -> Evolving<D>
) : Evolving<D> = when( conditions.ok() ) {
    false -> scope.etaEvolving( initialData )
    true -> {
        val evolvedData: D = scope.flow ( initialData ).get( )
        evolveSuspended(
            evolvedData,
            conditions.update( evolvedData ),
            scope
        ){
            data  -> flow ( data )
        }
    }
}

/**
 * Evolution
 */
tailrec fun <D,T> evolveSeq(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> D
) : D = when( conditions.ok() ) {
    false -> initialData
    true -> {
        val evolvedData: D = flow ( initialData )
        evolveSeq(
            evolvedData,
            conditions.update( evolvedData )
        ){
                data -> flow ( data )
        }
    }
}
/*
suspend fun <D, T> muEvolve(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    scope: CoroutineScope = DefaultEvolutionScope(),
    flow: suspend CoroutineScope.(D)->Evolving<D>
): Evolving<D> = when(conditions.ok()) {
    false -> etaEvolving(initialData)
    true -> scope.flow(initialData).map(scope){ data ->
        muEvolve(
            data,
            conditions.update(data)
        ) {
                d -> flow(d)
        }
    }.mu()
}

suspend fun <D, T> mEvolve(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    scope: CoroutineScope = DefaultEvolutionScope(),
    flow: suspend CoroutineScope.(D)->Evolving<D>
): Evolving<D> = when(conditions.ok()) {
    false -> etaEvolving(initialData)
    true -> scope.flow(initialData).map(scope){ data ->
        mEvolve(
            data,
            conditions.update(data)
        ) {
                d -> flow(d)
        }
    }.mu()
}

tailrec suspend fun <D, T> evolveMonadic(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    scope: CoroutineScope = DefaultEvolutionScope(),
    flow: suspend CoroutineScope.(D)->Evolving<D>
): Evolving<D> = when(conditions.ok()) {
    false -> etaEvolving(initialData)
    true -> {
        val evolvedData = scope.flow(initialData).get()
        evolveMonadic(
            evolvedData,
            conditions.update(evolvedData)
        ) {
                data -> flow( data )
        }
    }
}
*/

/*
tailrec suspendOnScope fun <D,T> coEvolve(
    initialData: Evolving<D>,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> D

): Evolving<D> {
    val evolvedData = initialData.get()
    val conditions = conditions.update(evolvedData)
    if(conditions.ok()) {

    }
}
        */