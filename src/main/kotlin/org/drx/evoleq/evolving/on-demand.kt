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
 * Todo test
 */
class OnDemand<D>(
    val scope: CoroutineScope = DefaultEvolvingScope(),
    private val default: D? = null,
    private val block: suspend CoroutineScope.() -> D
) : Evolving<D>, Cancellable<D> {
    var result : D? = null

    override val job: Job = scope.launch {
        while(result == null){delay(1)}
    }
    override suspend fun get(): D  = coroutineScope{
        scope + job
        scope + this.coroutineContext
        result = block()
        result!!
    }

    override fun cancel(d: D): Evolving<D> {
        scope.cancel()
        return object : Evolving<D> {
            override val job: Job
                get() = this@OnDemand.job

            override suspend fun get(): D = d
        }
    }
}


typealias LazyOnDemand<D> = CoroutineScope.(D)->OnDemand<D>