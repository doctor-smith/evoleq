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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.standard
import org.drx.evoleq.coroutines.suspendOnScope
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.drx.evoleq.evolving.Parallel
import org.junit.Test

class ParallelMathTest {
    @Test fun mapParallelStandard() = runBlocking {
        val scope = DefaultEvolvingScope()
        val startTime = System.currentTimeMillis()
        val parallel = scope.parallel(default = 0){
            delay(10_000)
            1
        }

        val mapped: Parallel<String> = parallel mapParallel standard{ x -> "$x" }
        assert(System.currentTimeMillis() - startTime < 5_000)


        parallel.job.cancel()
        delay(500)
        assert(mapped.job.isCancelled || mapped.job.isCompleted)
    }

    @Test fun mapParallelSuspendOnScope() = runBlocking {
        val scope = DefaultEvolvingScope()
        val startTime = System.currentTimeMillis()
        val parallel = scope.parallel(default = 0){
            delay(10_000)
            1
        }

        val mapped: Parallel<String> = parallel mapParallel suspendOnScope{ x -> "$x" }
        assert(System.currentTimeMillis() - startTime < 5_000)


        parallel.job.cancel()
        delay(1000)
        assert(mapped.job.isCancelled || mapped.job.isCompleted)
    }

    @Test fun mapParallelSuspended() = runBlocking {
        val scope = DefaultEvolvingScope()
        val startTime = System.currentTimeMillis()
        val parallel = scope.parallel(default = 0){
            delay(10_000)
            1
        }

        val mapped: Parallel<String> = parallel mapParallel suspended{ x -> "$x" }
        assert(System.currentTimeMillis() - startTime < 5_000)


        parallel.job.cancel()
        delay(500)
        assert(mapped.job.isCancelled || mapped.job.isCompleted)
    }

    //@Test
    fun muParallel() = runBlocking{
        val scope = DefaultEvolvingScope()
        var innerParallel: Parallel<Int>? = null
        val parallel = scope.parallel {
            innerParallel = parallel{
                delay(1_000)
                1
            }
            innerParallel!!
        }
        delay(100)
        assert(innerParallel!!.job.isActive)
        val mu = muParallel(parallel)
        delay(100)
        assert(mu == innerParallel!!)


        mu.job.cancel()

        delay(500)

        //assert(parallel.job.isCompleted)

        assert(innerParallel!!.job.isCancelled)


    }

    @Test fun fishParallel() = runBlocking {
        val scope = DefaultEvolvingScope()

        val f = suspended {x:Int -> scope.parallel {
            delay(10_000)
            x*x
        }
        }
        val g = suspended {x: Int->scope.parallel {
            delay(1_000)
            "$x"
        }
        }
        val startTime = System.currentTimeMillis()

        val h = f fishParallel g

        assert(System.currentTimeMillis() - startTime < 5_000)

    }

    //@Test
    fun fishParallelCancel() = runBlocking {
        val scope = DefaultEvolvingScope()

        var parallel1: Parallel<Int>? = null
        var parallel2: Parallel<String>? = null


        val f = suspended{x:Int ->
            parallel1 = scope.parallel {
                println("parallel_1")
                delay(2_000)
                x*x
            }
            parallel1!!
        }
        val g = suspended{x: Int->
            parallel2 = scope.parallel {
                println("parallel_2")
                delay(2_000)
                "$x"
            }
            parallel2!!
        }


        val h = f fishParallel g
        delay(100)
        h(2).job.cancel()
        //scope.cancel()
        delay(100)
        assert(parallel1!!.job.isCancelled)
        //assert(parallel2!!.job.isCancelled)

    }

}