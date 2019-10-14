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
package org.drx.evoleq.gap

import kotlinx.coroutines.*
import org.drx.evoleq.coroutines.onScope
import org.drx.evoleq.coroutines.suspendOnScope
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.junit.Test

class LazyGapTest {

    @Test fun cancellation() = runBlocking {
        val from = suspendOnScope {x: Int -> parallel{"$x"}  }
        val to = suspendOnScope { x: Int -> onScope{s:String -> parallel{x + Integer.parseInt(s)}} }

        val gap = LazyGap(from,to)

        var job :Job? = null
        var sc : CoroutineScope? = null
        val filler = onScope { s: String -> parallel{
            //job = this.coroutineContext[Job]
            sc = this
            delay(5_000)
            println(s+s)
            s+s
        } }

        val f = gap.fill(filler)
        println("here")
        val scope = DefaultEvolvingScope()
        val time = System.currentTimeMillis()
        parallel {
            delay(1_000)
            assert(sc!!.isActive)
            scope.cancel()//.coroutineContext[Job]!!.cancel()
            delay(1_000)
            assert(!scope.coroutineContext[Job]!!.isActive)
            assert(scope.coroutineContext[Job]!!.isCompleted)
        }
        parallel {
            scope.parallel {
                println("launched")
                f(1)

            }
        }

        assert(System.currentTimeMillis() - time <= 4_000)
        Unit
    }

}