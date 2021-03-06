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
import junit.framework.Assert.fail
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.standard
import org.drx.evoleq.dsl.gap
import org.drx.evoleq.dsl.initialSideEffect
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
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
        val scope = DefaultEvolvingScope()
        val property = SimpleObjectProperty<String>()
        val sideEffect: CoroutineScope.()->InitialSideEffect<String?> = initialSideEffect { property.value }
        class Data(val x: Int, val s: String)

        val gap = gap<Data,String?>{
            from{ scope.parallel{null} }
            to{
                data, s -> scope.parallel{Data(data.x, s!!)}
            }
        }

        val closed = close (gap) with { scope.parallel{ sideEffect()() } }

        property.value = "set"

        val result = closed(Data(0, "")).get()
        println("here")
        assert(result.s == "set")

        Unit
    }

    @Test fun gapCancellation() {
        fail("Not implemented")
    }

    @Test fun deepenGap() = runBlocking{
        //fail("Not implemented")

        val gap1 = gap<String,Int> {
            from { x: String -> parallel { x.length } }
            to { x: String, y: Int -> parallel { x + y } }
        }
        val gap2= gap<Int, Boolean> {
            from{ x-> parallel{x > 0}}
            to{x ,y -> parallel{ x + when(y){true -> 1; false -> 0} }}
        }

        val deepened = gap1.deepen(gap2)

        val filler: (Boolean) -> Evolving<Boolean> = { b:Boolean -> parallel {  !b }}
        val filled = deepened.fill (filler)

        val res = filled("aaa").get()

        assert(res == "aaa3")

    }

    @Test fun deepenGap2() = runBlocking {
        data class P(val x:String,  val q: Int)
        data class W(val a: Boolean, val p: P)

        val gap1 = gap<W,P>{
            from{ w:W -> parallel{ w.p } }
            to{ w: W, p:P -> parallel{ W(w.a,p) } }
        }

        val gap2 = gap<P,Int> {
            from{p -> parallel{p.q}}
            to{p,q -> parallel{P(p.x,q)}}
        }

        val deepened = gap1.deepen(gap2)

        val filler: (Int)->Evolving<Int> = {x -> parallel{x+1}}

        val filled = deepened.fill(standard{ x: Int -> parallel{x+1}})

        val result = filled(W(true,P("name", 0))).get()

        assert(result.a)
        assert(result.p.x == "name")
        assert(result.p.q == 1)


    }

    @Test fun widenGap() = runBlocking{
        data class P(val x:String,  val q: Int)
        data class W(val a: Boolean, val p: P)

        val gap1 = gap<W,P>{
            from{ w:W -> parallel{ w.p } }
            to{ w: W, p:P -> parallel{ W(w.a,p) } }
        }

        val gap2 = gap<P,Int> {
            from{p -> parallel{p.q}}
            to{p,q -> parallel{P(p.x,q)}}
        }

        val widened = gap2.widen(gap1)

        val filled = widened.fill(standard{x: Int -> parallel{x+1}})

        val result = filled(W(true,P("name", 0))).get()

        assert(result.a)
        assert(result.p.x == "name")
        assert(result.p.q == 1)
    }
}