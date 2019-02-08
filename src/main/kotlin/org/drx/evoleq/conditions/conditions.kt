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
package org.drx.evoleq.conditions


/**
 * Evolution conditions for the evolution equation
 */
data class EvolutionConditions<in D, T>(
    val testObject: T,
    val check:(T)->Boolean,
    val updateCondition: (D) -> T
)

/**
 * Update the evolution condition
 */
fun<D,T> EvolutionConditions<D, T>.update(data: D): EvolutionConditions<D, T> =
    copy( testObject = updateCondition( data ) )

/**
 * Check test-object
 */
fun<D,T> EvolutionConditions<D, T>.ok(): Boolean = check( testObject )

/**
 * Run flow only once
 */
fun <D> once(): EvolutionConditions<D, Boolean> =
    EvolutionConditions(
        testObject = true,
        check = { b -> b },
        updateCondition = { d: D -> false }
    )

/**
 * Run flow for a given amount of times
 */
class Counter<D>(private val to: Long) {
    fun get(): EvolutionConditions<Pair<D, Long>, Long> =
        EvolutionConditions(
            testObject = 0L,
            check = { l -> l < to }
        ) {
            pair -> pair.second
        }
}

fun <D> counting(f: (D)->D): (Pair<D,Long>)->Pair<D,Long> = {
    pair -> Pair(f(pair.first), pair.second +1)
}


