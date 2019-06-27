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

import kotlinx.coroutines.*
import org.drx.evoleq.dsl.asynq
import org.drx.evoleq.dsl.immediate
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.LazyEvolving
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.stub.ID
import org.drx.evoleq.stub.Stub
import kotlin.reflect.KClass

interface Evolver<D> {
    val scope: CoroutineScope
    suspend fun evolve(d: D): Evolving<D>
}


interface LazyEvolver<D> : Evolver<D> {
    suspend fun lazy(): LazyEvolving<D>
    override suspend fun evolve(d: D): Evolving<D> = lazy()(scope,d)
}

fun <D> LazyEvolver<D>.changeScope(scope: CoroutineScope): LazyEvolver<D> = object: LazyEvolver<D> {
    override val scope: CoroutineScope
        get() = scope

    override suspend fun lazy(): LazyEvolving<D> = lazy()
}



/**
 * Cancel evolver
 */
fun <D> Evolver<D>.cancel(cause: CancellationException? = null) = scope.cancel(cause)

suspend fun <D,E> Evolver<D>.forkImmediate(other: Evolver<E>) : Evolver<Pair<D,E>> = object: Evolver<Pair<D,E>> {

    val s = CoroutineScope(Job())
    init{
        s+this@forkImmediate.scope.coroutineContext
        s+other.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: Pair<D, E>): Evolving<Pair<D, E>> = scope.immediate {
        val first =this@forkImmediate.evolve(d.first)
        val second = other.evolve(d.second)
        Pair(first.get(),second.get())
    }
}
suspend fun <D,E> Evolver<D>.forkParallel(other: Evolver<E>) : Evolver<Pair<D,E>> = object: Evolver<Pair<D,E>> {
    val s = CoroutineScope(Job())
    init{
        s+this@forkParallel.scope.coroutineContext
        s+other.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: Pair<D, E>): Evolving<Pair<D, E>> = scope.parallel {
        val first =this@forkParallel.evolve(d.first)
        val second = other.evolve(d.second)
        Pair(first.get(),second.get())
    }
}
/*
suspendOnScope fun <D> Evolver<D>.forkParallel(vararg others: Evolver<D>) : Evolver<ArrayList<D>> = object: Evolver<ArrayList<D>> {
    override suspendOnScope fun evolve(d: ArrayList<D>): Evolving<ArrayList<D>> = Parallel{
        val result = arrayListOf(this@forkParallel.evolve(d.first()).get())
        others.forEachIndexed { index, evolver -> result.add(
            evolver.evolve(d[index +1]).get()
        ) }

        result
    }
}
*/
suspend fun <D,E> Evolver<D>.forkAsync(other: Evolver<E>) : Evolver<Pair<D,E>> = object: Evolver<Pair<D,E>> {
    val s = CoroutineScope(Job())
    init{
        s+this@forkAsync.scope.coroutineContext
        s+other.scope.coroutineContext
    }
    override val scope: CoroutineScope
        get() = s
    override suspend fun evolve(d: Pair<D, E>): Evolving<Pair<D, E>> = scope.asynq {
        val first =this@forkAsync.evolve(d.first)
        val second = other.evolve(d.second)
        Pair(first.get(),second.get())
    }
}

data class ErrorCatcher<D>(val data: D, val error: Exception? = null)
fun <D> Evolver<D>.runCatchingErrors() : Evolver<ErrorCatcher<D>> = object: Evolver<ErrorCatcher<D>> {
    override val scope: CoroutineScope
        get() = this@runCatchingErrors.scope
    override suspend fun evolve(d: ErrorCatcher<D>): Evolving<ErrorCatcher<D>> = scope.immediate {
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


fun <D> Evolver<D>.toStub(id: ID = Evolver::class, stubs: HashMap<ID, Stub<*>> = HashMap()) : Stub<D> = object: Stub<D> {
    override val scope: CoroutineScope
        get() = this@toStub.scope
    override val id: KClass<*>
        get() = id
    override val stubs: HashMap<KClass<*>, Stub<*>>
        get() = stubs

    override suspend fun evolve(d: D): Evolving<D> {
        return this@toStub.evolve(d)
    }
}
