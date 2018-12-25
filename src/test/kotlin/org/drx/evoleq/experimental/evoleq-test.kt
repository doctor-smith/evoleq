/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.experimental

import kotlinx.coroutines.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.evolve
import org.junit.Test

typealias Data = Pair<Int,Int>

class EvolEqTest{
    @Test
    fun test1()
    {
        val N = 10
        val data = Data(0, 0)
        val conditions = EvolutionConditions<Data, Int>(
            testObject = 0,
            check = { x -> x <= N },
            updateCondition = { newData -> newData.second }
        )
        val f: (Data) -> Evolving<Data> = { oldData ->
            Parallel {
                Thread.sleep(100)
                val newData = Data(oldData.first + oldData.second, oldData.second + 1)
                //println(newData.first)
                newData
            }
        }
        GlobalScope.async {
            val newData = evolve(data, conditions) { data ->
                f(data)
            }
            assert(newData.first == N*(N + 1)/2)
        }

        Thread.sleep(1_000)
    }


}