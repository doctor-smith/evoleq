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
package org.drx.evoleq.old

import kotlinx.coroutines.Deferred
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.conditions.ok
import org.drx.evoleq.evolving.parallel
import org.drx.evoleq.conditions.update

/**
 * Old but nice
 * ====================================================================================================================
 */
/**
 * Old but nice - but not tailrec
 */
suspend fun <D, T> evolveOld1(
    data: D,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> Deferred<D>
): D = when ( conditions.ok() ) {
    false -> data
    true -> parallel {
        //  <- This makes it non-tail-recursive
        val newData: D = flow(data).await()
        evolveOld1(
            newData,
            conditions.update(newData)
        ) { data ->
            flow(data)
        }
    }
}

/**
 * Old but nice - and equivalent to the version old1
 */
suspend fun <D,T> evolveOld0(
    data: D,
    testObject: T,
    condition:(T)->Boolean,
    updateCondition: (D)-> T,
    flow: (D)-> Deferred<D>
): D = when ( condition( testObject ) ) {
    false -> data
    true -> parallel {
        val newData: D = flow(data).await()
        val newTestObject: T = updateCondition(newData)
        evolveOld0(
            newData,
            newTestObject,
            condition,
            updateCondition
        ) { data ->
            flow(data)
        }
    }
}

