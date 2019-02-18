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
package org.drx.evoleq

import kotlinx.coroutines.*
import org.drx.evoleq.conditions.once
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.math.times
import org.drx.evoleq.flow.Flow
import org.drx.evoleq.flow.repeatImmediate
import org.junit.Test

class EvolvingTest {

    @Test
    fun immediateDoesNotBreakParallel() = runBlocking{
        /* TODO find good test */
    }

    @Test
    fun testTimes() {
        val f = {s:String->
            Parallel<Int> {
                delay(1_000)
                s.length
            }
        }
        val g = {i:Int ->
            Parallel<Double> {
                i.toDouble()
            }
        }
        runBlocking {
            val h = Parallel<(String)-> Evolving<Double>> { f * g }.get()
            val j = f*g
            assert(h("kind").get() == 4.0)
            assert(j("kind").get() == 4.0)
        }
        var x =2.0
        GlobalScope.launch { (f*g)("1") }.invokeOnCompletion {  }
    }


    @Test
    fun testRepetition() {
        runBlocking {
            val f = {s: Int -> s + 1}
            val x = repeatImmediate(10, 0, f)
            assert(x == 10)
        }
    }

    @Test
    fun testBackPropagationFirstAttempt() {

        val stub = object{
            fun get(): Int = 3
        }

        runBlocking {
            /* TODO implement */
            val flow = Flow(
                once()
            ) { pair: Pair<Int, (Int) -> Int> ->
                Immediate {

                    val x = repeatImmediate(5, pair.first, pair.second)

                    Pair(x, pair.second)
                }
            }

            assert(flow.evolve(Pair(0, {s:Int->s+1})).get().first == 5)
            assert (stub.get() == 3)
        }
    }


}