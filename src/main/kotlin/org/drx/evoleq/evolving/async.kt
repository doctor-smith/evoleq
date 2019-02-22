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

class Async<D>(
            private val delay: Long = 1,
            val scope: CoroutineScope = GlobalScope,
            private val block: suspend Async<D>.() -> D
) : Evolving<D>, Cancellable<D> {

    private lateinit var deferred: Deferred<D>

    private var default: D? = null

    init {
        scope.launch {
            deferred = async { this@Async.block() }
        }
    }

    override suspend fun get(): D {
        while (!::deferred.isInitialized) {
            delay(delay)
        }

        return blockRace(
            scope,
            { deferred.await() },
            {
                while (default == null) {
                    delay(1)
                }
                default!!
            }
        ).await()
    }

    override fun cancel(d: D): Evolving<D> = Immediate {
        default = d
        if (::deferred.isInitialized) {
            deferred.cancel()
        } else {
            coroutineScope {
                var cnt = 0
                while (cnt < 1000 && !::deferred.isInitialized) {
                    delay(delay)
                    cnt++
                }
                deferred.cancel()
            }
        }

        d
    }

    fun job(): Job = deferred
}