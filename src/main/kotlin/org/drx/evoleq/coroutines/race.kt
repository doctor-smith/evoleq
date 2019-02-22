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
package org.drx.evoleq.coroutines

import kotlinx.coroutines.*

suspend fun <D> blockRace(scope: CoroutineScope = GlobalScope, block1: suspend ()->D, block2: suspend ()->D): Deferred<D> = scope.async {
    lateinit var job1: Job
    var job2: Job? = null

    var result: D? = null

    job1 = scope.launch{ coroutineScope{
        result = block1()
        while(job2 == null) {
            delay(1)
        }
        job2!!.cancel()
    }}
    job2 = scope.launch{ coroutineScope{
        result = block2()

        job1.cancel()
    }}
    while(result == null) {
        delay(1)
    }
    result!!
}