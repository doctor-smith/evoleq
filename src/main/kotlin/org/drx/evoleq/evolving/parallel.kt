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
package org.drx.evoleq.evolving

import kotlinx.coroutines.*
import org.drx.evoleq.coroutines.blockRace
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Evolution type parallel:
 * evolve async and parallel
 * - without blocking the current thread
 */
class Parallel<D>(
    private val delay: Long = 1,
    val scope: CoroutineScope = GlobalScope,//CoroutineScope(EmptyCoroutineContext),
    private val block: suspend CoroutineScope.() -> D
) : Evolving<D>, Cancellable<D> {

    private lateinit var deferred: Deferred<D>

    private var default: D? = null

    private var job: Job


    init {
        job = scope.launch {
         coroutineScope {
                deferred = async { block() }
                default = deferred.await()
            }
        }
        scope + job

    }

    override suspend fun get(): D = coroutineScope {
        while(default == null){
            delay(delay)
        }
        default!!
    }

    override fun cancel(d: D): Evolving<D> = object: Evolving<D> {
        init {
            default = d
            if (::deferred.isInitialized) {
                deferred.cancel()
            }
            job.cancel()
        }

        override suspend fun get(): D = d
    }

    fun job(): Job = deferred

}
