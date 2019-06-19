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

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.gap
import org.drx.evoleq.dsl.initialSideEffect
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.sideeffect.InitialSideEffect
import org.drx.evoleq.sugar.close
import org.drx.evoleq.sugar.with
import org.junit.Test

class GapTest {

    @Test fun pairGap() = runBlocking {
        class Data(val x: Int, val s: String)

        val filler: (String)-> Evolving<String> = { s -> Immediate{s+s}}
        val gap = gap<Data, String>{
            from{
                data: Data -> Immediate{data.s}
            }
            to{
                data: Data, s: String -> Immediate{Data(data.x,s)}
            }
        }

        val filled = gap.fill(filler)

        val res = filled(Data(0, "xy")).get()

        assert(res.x == 0)
        assert(res.s =="xyxy")
    }


    @Test fun historyGap() = runBlocking{
        class Data(val x: Int, val s: String, val history: ArrayList<Data> = arrayListOf())

        val filler: (String)-> Evolving<String> = { s -> Immediate{s+s}}

        val gap = gap<Data, String> {
            from{
                data -> Immediate{ data.s }
            }
            to{
                data, s -> Immediate {
                    data.history.add(0,data)
                    Data(data.x, s, data.history)
                }
            }
        }
        val filled = gap.fill(filler)

        val data = Data(0,"xy")
        val result = filled(data).get()

        assert(result.x == 0)
        assert(result.s == "xyxy")
        val newHistory = result.history
        assert(newHistory.size == 1)
        assert(newHistory.first() == data)
    }

    //@Test
    fun sideEffectGap() = runBlocking{
        val property = SimpleObjectProperty<String>()
        val sideEffect: InitialSideEffect<String?> = initialSideEffect { property.value }
        class Data(val x: Int, val s: String)

        val gap = gap<Data,String?>{
            from{ Parallel{null} }
            to{
                data, s -> Parallel{Data(data.x, s!!)}
            }
        }

        val closed = close (gap) with { Parallel{ sideEffect() } }

        property.value = "set"

        val result = closed(Data(0,"")).get()

        assert(result.s == "set")
    }
}