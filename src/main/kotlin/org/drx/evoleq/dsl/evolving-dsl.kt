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
import kotlinx.coroutines.plus
import org.drx.evoleq.evolving.*

@EvoleqDsl
fun <D> CoroutineScope.parallel(
    delay: Long = 1,
    default: D? = null,
    block: suspend CoroutineScope.() -> D
): Parallel<D> = Parallel(delay,this, default ){block()}

fun <D> lazyParallel(
    delay: Long = 1,
    block: suspend CoroutineScope.(D) -> D
): LazyParallel<D>  = {
    parallel(delay, it) {
         block(it)
    }
}
@EvoleqDsl
fun <D> CoroutineScope.asynq(
    delay: Long = 1,
    default: D? = null,
    block: suspend CoroutineScope.() -> D
): Async<D> = Async(delay,this, default){block()}

fun <D> lazyAsync(
    delay: Long = 1,
    block: suspend CoroutineScope.(D) -> D
): LazyAsync<D>  = {
    asynq(delay, it) {
        block(it)
    }
}

fun <D> lazyImmediate(
    block: CoroutineScope.(D) -> D
): LazyImmediate<D> = {
    immediate{
        block(it)
    }
}

fun <D> lazyOnDemand(
    block: suspend CoroutineScope.(D) -> D
): LazyOnDemand<D>  = {
    onDemand(it) {
        block(it)
    }
}
@EvoleqDsl
fun <D> CoroutineScope.immediate(default: D? = null, block:  CoroutineScope.()->D) = Immediate(this){ block() }
@EvoleqDsl
fun <D> CoroutineScope.onDemand(default : D? = null, block: suspend CoroutineScope.()->D) = OnDemand(this){ block() }

fun <D,E> Evolving<D>.parallel(delay: Long = 1,default: E? = null, block: suspend CoroutineScope.() -> E): Parallel<E> = Parallel(delay, CoroutineScope(this.job),default) { block() }

fun <D,E> Evolving<D>.async(delay: Long = 1,default: E? = null, block: suspend CoroutineScope.() -> E): Async<E> = Async(delay, CoroutineScope(this.job),default) { block() }

fun <D,E> Evolving<D>.immediate(block: CoroutineScope.() -> E): Immediate<E> = Immediate(CoroutineScope(this.job)) { block() }

fun <D,E> Evolving<D>.onDemand(block: suspend CoroutineScope.() -> E): OnDemand<E> = OnDemand(CoroutineScope(this.job)) { block() }

/**
 * Force an evolving to run under a new scope
 */
fun <D> Evolving<D>.onScope(scope: CoroutineScope): Evolving<D> = object: Evolving<D> {

    val s: CoroutineScope = scope+this@onScope.job

    override val job: Job
        get() = s.coroutineContext[Job]!!

    override suspend fun get(): D = this@onScope.get()
}