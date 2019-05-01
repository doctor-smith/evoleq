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

import org.drx.evoleq.evolving.Async
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill

interface Evolver<D> {
    suspend fun evolve(d: D): Evolving<D>
}


suspend fun <D,E> Evolver<D>.forkImmediate(other: Evolver<E>) : Evolver<Pair<D,E>> = object: Evolver<Pair<D,E>> {
    override suspend fun evolve(d: Pair<D, E>): Evolving<Pair<D, E>> = Immediate {
        val first =this@forkImmediate.evolve(d.first)
        val second = other.evolve(d.second)
        Pair(first.get(),second.get())
    }
}
suspend fun <D,E> Evolver<D>.forkParallel(other: Evolver<E>) : Evolver<Pair<D,E>> = object: Evolver<Pair<D,E>> {
    override suspend fun evolve(d: Pair<D, E>): Evolving<Pair<D, E>> = Parallel {
        val first =this@forkParallel.evolve(d.first)
        val second = other.evolve(d.second)
        Pair(first.get(),second.get())
    }
}
/*
suspend fun <D> Evolver<D>.forkParallel(vararg others: Evolver<D>) : Evolver<ArrayList<D>> = object: Evolver<ArrayList<D>> {
    override suspend fun evolve(d: ArrayList<D>): Evolving<ArrayList<D>> = Parallel{
        val result = arrayListOf(this@forkParallel.evolve(d.first()).get())
        others.forEachIndexed { index, evolver -> result.add(
            evolver.evolve(d[index +1]).get()
        ) }

        result
    }
}
*/
suspend fun <D,E> Evolver<D>.forkAsync(other: Evolver<E>) : Evolver<Pair<D,E>> = object: Evolver<Pair<D,E>> {
    override suspend fun evolve(d: Pair<D, E>): Evolving<Pair<D, E>> = Async {
        val first =this@forkAsync.evolve(d.first)
        val second = other.evolve(d.second)
        Pair(first.get(),second.get())
    }
}

data class ErrorCatcher<D>(val data: D, val error: Exception? = null)
fun <D> Evolver<D>.runCatchingErrors() : Evolver<ErrorCatcher<D>> = object: Evolver<ErrorCatcher<D>> {
    override suspend fun evolve(d: ErrorCatcher<D>): Evolving<ErrorCatcher<D>> = Immediate {
        try{
            ErrorCatcher(this@runCatchingErrors.evolve(d.data).get())
        } catch(exception : Exception){
            d.copy(error = exception)
        }
    }
}

suspend fun <D,E> D.intercept(with: Evolver<E>, gap: Gap<D, E>): Evolving<D> {
    val f: suspend (E)-> Evolving<E> = {e: E -> with.evolve(e)}
    return gap.fill(f)(this)
}
