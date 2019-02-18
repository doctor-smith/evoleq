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
package org.drx.evoleq.flow

import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Async
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.junit.Test

class FlowTest {

    @Test
    fun parallelFlow() = runBlocking{
        val flow = flow<Int, Boolean> {
            setupConditions{
                testObject( true )
                check{b: Boolean -> b}
                updateCondition { x: Int -> x <= 0 }
            }
            flow  {
                x:Int -> Immediate{
                    val p1 = Parallel<Int>{
                        delay(1_000)
                        1
                    }
                    val p2 = Parallel<Int>{
                        kotlinx.coroutines.delay(1_000)
                        2
                    }
                    val p3 = Parallel<Int>{
                        kotlinx.coroutines.delay(1_000)
                        3
                    }
                    x + p1.get()+p2.get()+p3.get()
                }
            }
        }

        //delay(1_000) // wait for config
        var time = System.currentTimeMillis()
        val res = flow.evolve(0).get()
        time = System.currentTimeMillis() - time
        assert(res == 6)
        assert(time >= 1_000)
        assert(time <= 1_500)
        println(res)

    }

    @Test
    fun asyncFlow() = runBlocking{
        val flow = flow<Int, Boolean> {
            conditions(conditions{
                testObject( true )
                check{b: Boolean -> b}
                updateCondition { x: Int -> x <= 0 }
            })
            flow { x:Int -> Immediate{
                val p1 = Async<Int>{
                    delay(1_000)
                    1
                }
                val p2 = Async<Int>{
                    delay(1_000)
                    2
                }
                val p3 = Async<Int>{
                    delay(1_000)
                    3
                }
                x + p1.get()+p2.get()+p3.get()
            } }
        }

        //delay(1_000) // wait for config
        var time = System.currentTimeMillis()
        val res = flow.evolve(0).get()
        time = System.currentTimeMillis() - time
        assert(res == 6)
        assert(time >= 1_000)
        assert(time <= 1_500)

    }

    @Test fun parallelFlows() = runBlocking{
        class Data(val x:Int, val s: String)
        val property = SimpleIntegerProperty(-1)
        var changes = 0
        property.addListener{_,oV,nV ->
            if(oV == -1) {
                //println("initial change")
            }
            else if(oV != nV){
                //println("change: oV = $oV; nV = $nV" )
                changes ++
            }
        }
        val flow = flow<Data, Boolean>{
            conditions(once())
            flow{
                data -> Immediate{
                    val parallel1 = Parallel<Int>{
                        suspendedFlow<Int, Boolean> {
                            setupConditions{
                                testObject(true)
                                check{b-> b}
                                updateCondition{x -> x <= 10}
                            }
                            flow{
                                x -> Immediate{
                                    delay(2)
                                    property.value = 1
                                    x + 1
                                }
                            }
                        }.evolve(data.x).get()
                    }
                    val parallel2 = Parallel<String> {
                        suspendedFlow<String, Boolean> {
                            setupConditions{
                                testObject(true)
                                check{b-> b}
                                updateCondition{x -> x.length <= 1024}
                            }
                            flow{
                                s -> Immediate{
                                    delay(2)
                                    property.value = 2
                                    "$s-+$s"
                                }
                            }
                        }.evolve(data.s).get()
                    }
                    Data( parallel1.get(), parallel2.get() )
                }
            }
        }

        // result doesn't matter
        flow.evolve(Data(0, "1")).get()

        assert(changes >= 2)
    }



}