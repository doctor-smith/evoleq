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

/**
 * Immediate: return immediately, blocking the current thread
 */
class Immediate<D>(val scope: CoroutineScope = DefaultEvolvingScope(), val default: D? = null, private val block: CoroutineScope.()->D) : Evolving<D>, Cancellable<D> {
    private var set = false
    private var result: D? = null

    override val job: Job

    init{
        job = scope.launch { coroutineScope {
            while (result == null) {
                //println("Immediate: result = null")
                delay(1)
            }
            set = true
        } }

        job.invokeOnCompletion{
            if(result == null){
            result = default
            set = true
        } }

        //GlobalScope.launch{ coroutineScope{
            scope + job
            result = scope.block()
        //} }
        // delay initialization until result is computed

        /*
        while(!set){

            sleep(1)
        }

         */
    }

    @Suppress("unchecked_cast")
    override suspend fun get(): D = coroutineScope {
        while(!set){
            delay(1)
        }
        //println("block executed\n")
        result!!
    }

    override fun cancel(d: D): Evolving<D> {
        scope.cancel()
        return object : Evolving<D> {
            override val job: Job
                get() = this@Immediate.job

            override suspend fun get(): D = d
        }
    }
}

typealias LazyImmediate<D> = suspend CoroutineScope.(D)->Immediate<D>