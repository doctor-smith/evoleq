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
package org.drx.evoleq.math

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.plus
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.data.*
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Evolver

operator fun <D,E> Evolver<D>.plus(other: Evolver<E>): Evolver<Sum<D,E>> = object: Evolver<Sum<D,E>> {
    private val s: CoroutineScope = CoroutineScope(Job())
    init{
        s+this@plus.scope.coroutineContext
        s+other.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: Sum<D, E>): Evolving<Sum<D, E>> =when(d) {
        is Sum.First ->  this@plus.evolve(d.value) map suspended{ v: D -> Sum.First<D,E>(v) }
        is Sum.Second ->  other.evolve(d.value) map suspended{ v: E -> Sum.Second<D,E>(v) }
    }
}

fun <D,E,F> sum(first: Evolver<D>, second: Evolver<E>, third: Evolver<F>): Evolver<TripleSum<D,E,F>> = object: Evolver<TripleSum<D,E,F>> {
    private val s: CoroutineScope = CoroutineScope(Job())
    init{
        s+first.scope.coroutineContext
        s+second.scope.coroutineContext
        s+third.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: TripleSum<D, E, F>): Evolving<TripleSum<D, E, F>> = when(d){
        is TripleSum.First -> first.evolve(d.value) map suspended { v -> TripleSum.First(v) }
        is TripleSum.Second -> second.evolve(d.value) map suspended { v -> TripleSum.Second(v) }
        is TripleSum.Third -> third.evolve(d.value) map suspended { v -> TripleSum.Third(v) }
    }
}

fun <D,E,F,G> sum(first: Evolver<D>, second: Evolver<E>, third: Evolver<F>, fourth: Evolver<G>): Evolver<QuadSum<D, E, F, G>> = object: Evolver<QuadSum<D,E,F,G>> {
    private val s: CoroutineScope = CoroutineScope(Job())
    init{
        s+first.scope.coroutineContext
        s+second.scope.coroutineContext
        s+third.scope.coroutineContext
        s+fourth.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: QuadSum<D, E, F, G>): Evolving<QuadSum<D, E, F, G>> = when(d){
        is QuadSum.First -> first.evolve(d.value) map suspended { v -> QuadSum.First(v) }
        is QuadSum.Second -> second.evolve(d.value) map suspended { v -> QuadSum.Second(v) }
        is QuadSum.Third -> third.evolve(d.value) map suspended { v -> QuadSum.Third(v) }
        is QuadSum.Fourth -> fourth.evolve(d.value) map suspended { v -> QuadSum.Fourth(v) }
    }
}

fun <D,E,F,G,H> sum(first: Evolver<D>, second: Evolver<E>, third: Evolver<F>, fourth: Evolver<G>, fith: Evolver<H>): Evolver<QuintSum<D, E, F, G, H>> = object: Evolver<QuintSum<D,E,F,G,H>> {
    private val s: CoroutineScope = CoroutineScope(Job())
    init{
        s+first.scope.coroutineContext
        s+second.scope.coroutineContext
        s+third.scope.coroutineContext
        s+fourth.scope.coroutineContext
        s+fith.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: QuintSum<D, E, F, G,H>): Evolving<QuintSum<D, E, F, G,H>> = when(d){
        is QuintSum.First -> first.evolve(d.value) map suspended { v -> QuintSum.First(v) }
        is QuintSum.Second -> second.evolve(d.value) map suspended { v -> QuintSum.Second(v) }
        is QuintSum.Third -> third.evolve(d.value) map suspended { v -> QuintSum.Third(v) }
        is QuintSum.Fourth -> fourth.evolve(d.value) map suspended { v -> QuintSum.Fourth(v) }
        is QuintSum.Fifth -> fith.evolve(d.value) map suspended { v -> QuintSum.Fifth(v) }
    }
}


fun <D,E,F> Evolver<Sum<Sum<D,E>,F>>.flatten(): Evolver<TripleSum<D,E,F>> = object: Evolver<TripleSum<D,E,F>> {
    override val scope: CoroutineScope
        get() = this@flatten.scope
    override suspend fun evolve(d: TripleSum<D, E, F>): Evolving<TripleSum<D, E, F>> = when(d) {
        is TripleSum.First -> this@flatten.evolve(Sum.First(Sum.First(d.value)))  map suspended{ v -> v.flatten() }
        is TripleSum.Second -> this@flatten.evolve(Sum.First(Sum.Second(d.value)))  map suspended{ v -> v.flatten() }
        is TripleSum.Third -> this@flatten.evolve(Sum.Second(d.value))  map suspended{ v -> v.flatten() }
    }
}


