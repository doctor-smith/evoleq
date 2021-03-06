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
import kotlinx.coroutines.*
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.*
import org.drx.evoleq.evolving.Async
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.stub.DefaultIdentificationKey
import org.drx.evoleq.stub.lazyStub
import org.drx.evoleq.stub.toFlow
import org.drx.evoleq.stub.toLazyFlow
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
                x:Int -> Parallel{
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
            flow { x:Int -> Parallel{
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
                println("initial change")
            }
            else if(oV != nV){
                println("change: oV = $oV; nV = $nV" )
                changes ++
            }
        }

        val scope1 = DefaultStubScope()
        val scope2 = DefaultStubScope()

        val flow = flow<Data, Boolean>{
            conditions(once())
            flow{
                data -> parallel{
                    println("launching main parallel")
                    val parallel1 = parallel<Int>{
                        println("   parallel 1")
                        suspendedFlow<Int, Boolean> {
                            setupConditions{
                                testObject(true)
                                check{b-> b}
                                updateCondition{x -> x <= 10}
                            }
                            flow{
                                x -> parallel{
                                    println("       flow 1")
                                    delay(2)
                                    property.value = 1
                                    x + 1
                                }
                            }
                        }.evolve(data.x).get()
                    }
                    val parallel2 = parallel<String> {
                        println("   parallel 2")
                        suspendedFlow<String, Boolean> {
                            setupConditions{
                                testObject(true)
                                check{b-> b}
                                updateCondition{x -> x.length <= 1024}
                            }
                            flow{
                                s -> parallel{
                                    println("       flow 2")
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
        val res = flow.evolve(Data(0, "1")).get()
        println("result: $res")
        assert(changes >= 2)
    }

    @Test fun cancelFlowWithScopeInheritedFromUnderlyingStub() = runBlocking {
        val mainScope = CoroutineScope(Job())
        var one: Parallel<String>? = null
        var two: Parallel<String>? = null
        val flow = mainScope.stub<String>{
            id(DefaultIdentificationKey::class)
            /*
            evolveLazy {
                string -> parallel(default = "CANCELLED") {
                    one = parallel{
                        println("one")
                        delay(10_000)
                        string
                    }
                    two = parallel{
                        println("two")
                        delay(10_000)
                        string
                    }
                    "STOP"
                }
            }

             */

            evolve{
                string -> scope.parallel(default = "CANCELLED") {
                    one = parallel{
                        delay(10_000)
                        string
                    }
                    two = parallel{
                        delay(10_000)
                        string
                    }
                    "STOP"
                }
            }


        }.toFlow<String,Boolean>(
            conditions{
                testObject(true)
                check{b->b}
                updateCondition { false }
            }
        )
        GlobalScope.parallel {
            flow.evolve("START")
        }
        delay(1500)
        assert(one!!.job.isActive)
        assert(two!!.job.isActive)
        flow.cancel()
        delay(1500)

        //assert(one!!.job.isActive)
        assert(one!!.job.isCancelled)
        assert(two!!.job.isCancelled)
    }

    @Test fun cancelNestedLazyFlows() = runBlocking {
        class Stub

        var job: Job? = null

        val lF0 = lazyStub<Int>(stub{
            id(Stub::class)
            evolveLazy { x -> this.parallel(default = x) {
                job = this.coroutineContext[Job]!!
                delay(1_000)
                x+1
            } }
        }).toLazyFlow<Int,Boolean>(
            conditions{
                testObject(true)
                check{b->b}
                updateCondition { it < 10 }
            }
        )

        val lF1 = lazyStub<Int>(stub{
            id(Stub::class)
            evolveLazy { x -> this.parallel(default = x) {
                lF0().evolve(x).get()
            } }
        }).toLazyFlow<Int,Boolean>(
            conditions{
                testObject(true)
                check{b->b}
                updateCondition { it < 10 }
            }
        )

        val scope = CoroutineScope(Job())

        scope.lF1().evolve(1)

        delay(100)
        scope.cancel()
        delay(100)

        assert(job!!.isCancelled)
    }

}