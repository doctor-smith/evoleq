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
package org.drx.evoleq.experimental.coroutine.coroutine1

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ParallelTest {
    @Test
    fun performance() = runBlocking {
        var time = System.currentTimeMillis()
        val parallel = Parallel<Int>{
            1
        }
        println(System.currentTimeMillis()-time)
        time = System.currentTimeMillis()
        val x = parallel.get()
        time = System.currentTimeMillis() - time
        println(time)
        time = System.currentTimeMillis()
        var time1 = System.currentTimeMillis()
        val parallel1 = Parallel<Int>{
            delay(1_000)
            1
        }
        time = System.currentTimeMillis() - time
        println(time)
        parallel1.cancel(2)
        time1 = System.currentTimeMillis() -time1
        println(time1)


    }

    @Test fun isParallelExecution() = runBlocking {
        var launchingTime = System.currentTimeMillis()
        var execTime = System.currentTimeMillis()
        val parallel = Parallel<Unit>{

            delay(1_000)
            Unit
        }
        launchingTime = System.currentTimeMillis() - launchingTime
        assert(launchingTime <= 100)
        val x = parallel.get()
        execTime = System.currentTimeMillis() - execTime
        assert(execTime >= 1_000)
    }
}