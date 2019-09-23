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
import org.drx.evoleq.dsl.immediate
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate


/**
 * Functoriality:
 * ==============
 */
suspend infix
fun <D1,D2> Evolving<D1>.map(f:  (D1) -> D2) : Evolving<D2> = object : Evolving<D2> {
    override val job: Job
        get() = this@map.job
    override suspend fun get(): D2 = f ( this@map.get() )
}
suspend infix
fun <D1,D2> Evolving<D1>.map(f: suspend (D1) -> D2) : Evolving<D2> = object : Evolving<D2> {
    override val job: Job
        get() = this@map.job
    override suspend fun get(): D2 = f ( this@map.get() )
}
suspend infix
fun<D1,D2> Evolving<D1>.lift(f: (D1) -> D2) : (Evolving<D1>) -> Evolving<D2> = { ev -> CoroutineScope(job).immediate { (ev map f).get() } }

suspend infix
fun<D1,D2> Evolving<D1>.lift(f: suspend (D1) -> D2) : (Evolving<D1>) -> Evolving<D2> = { ev -> CoroutineScope(job).immediate { (ev map f).get() } }
/**
 * Monad
 * =====
 */
/**
 * Enter the monad
 */
fun <D> etaEvolving(data: D): Evolving<D> = Immediate { data }

fun <D> CoroutineScope.etaEvolving(data: D): Evolving<D> = immediate { data }
/**
 * Multiply evolvings
 */
suspend fun <D> muEvolving(evolving: Evolving<Evolving<D>>): Evolving<D> {
    return evolving.get()
}

/**
 * Fish operator / multiplication on kleisli arrows
 */
suspend operator
fun <R,S,T> ( (R)-> Evolving<S>).times(flow: (S)-> Evolving<T>) : (R)-> Evolving<T> = {
        r ->
    Immediate { muEvolving(this@times(r) map flow).get() }
}
suspend operator
fun <R,S,T> ( suspend (R)-> Evolving<S>).times(flow: (S)-> Evolving<T>) : suspend (R)-> Evolving<T> = {
        r -> muEvolving(this@times(r) map flow)
}
suspend operator
fun <R,S,T> ( suspend (R)-> Evolving<S>).times(flow: suspend (S)-> Evolving<T>) :suspend (R)-> Evolving<T> = {
        r -> muEvolving(this@times(r) map flow)
}
suspend operator
fun <R,S,T> (  (R)-> Evolving<S>).times(flow: suspend (S)-> Evolving<T>) :suspend (R)-> Evolving<T> = {
        r -> muEvolving(this@times(r) map flow)
}

fun<S,T> klEvolving(f:(S)->T): (S)->Evolving<T> = {s->Immediate{f(s)}}

/**
 * Comonad
 * =======
 */
fun <D> deltaEvolving(ev: Evolving<D>): Evolving<Evolving<D>> =
    etaEvolving(ev)
/**
 * Bird operator on cokleisli arrows
 */
suspend operator
fun <R, S, T> ((Evolving<R>)->S).div(f:(Evolving<S>)->T): (Evolving<R>)->T = {
        evR -> f(etaEvolving(this(evR)))
}
suspend operator
fun <R, S, T> (suspend (Evolving<R>)->S).div(f:(Evolving<S>)->T):suspend (Evolving<R>)->T = {
        evR -> f(etaEvolving(this(evR)))
}
suspend operator
fun <R, S, T> ((Evolving<R>)->S).div(f:suspend (Evolving<S>)->T): suspend (Evolving<R>)->T = {
        evR -> f(etaEvolving(this(evR)))
}
suspend operator
fun <R, S, T> (suspend (Evolving<R>)->S).div(f: suspend (Evolving<S>)->T): suspend (Evolving<R>)->T = {
        evR -> f(etaEvolving(this(evR)))
}

suspend fun <S,T> coklEvolving(f:(S)->T): suspend (Evolving<S>)->T {
    val mapper : suspend (Evolving<S>)->Evolving<T> = {ev-> ev.map (f)  }
    val cokl: suspend (Evolving<S>)-> T =  { evolving -> mapper(evolving).get() }

    return cokl
}

suspend fun <S,T> coklEvolving(f: suspend (S)->T): suspend (Evolving<S>)->T {
    val mapper : suspend (Evolving<S>)->Evolving<T> = {ev-> ev.map (f)  }
    val cokl: suspend (Evolving<S>)-> T =  { evolving -> mapper(evolving).get() }

    return cokl
}


fun <D,E> Pair<Evolving<D>,Evolving<E>>.ev(): Evolving<Pair<D,E>> = object: Evolving<Pair<D,E>>{

    val scope = CoroutineScope(Job())+first.job+second.job

    override val job: Job
        get() = scope.coroutineContext[Job]!!

    override suspend fun get(): Pair<D, E> = Pair(first.get(), second.get())

}
