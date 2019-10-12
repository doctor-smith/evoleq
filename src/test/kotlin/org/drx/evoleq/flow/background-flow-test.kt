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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.onNext
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.stub.toFlow
import org.junit.Test

class BackgroundFlowTest {

    @Test fun simpleExample() = runBlocking{

        val max = 100

        class Data(val value : Int = 0)
        class Component : SimpleLiveCycleFlow<Int, Data>() {
            override fun initData(): Data = Data()

            override fun onInput(input: Int): Process.Phase.Runtime<Data> =
                if(input <= max ){
                    if(input == 40){
                        throw Exception("tralala")
                    } else {
                        Process.Phase.Runtime.Wait(Data(input))
                    }
                } else {
                    Process.Phase.Runtime.Stop(Data(input))
                }


            override fun onStart(data: Data): Data = data

            override fun onStop(data: Data): Data = data

            val flow = stub<Boolean>{
                id(Component::class)
                evolve{ bool -> when(bool) {
                    true -> scope.parallel{
                        outputStack.onNext { phase ->
                            when(phase) {
                                is Process.Output.Starting -> {
                                    println("background flow starting")
                                    true
                                }
                                is Process.Output.Waiting ->  {
                                    println("background flow waiting")
                                    IntRange(1,25).forEach {
                                        bgInput.send(phase.data.value + it)
                                    }
                                    true
                                }
                                is Process.Output.Error -> {
                                    println("error\n" + phase)
                                    bgInput.send(max + 1)
                                    true
                                }
                                is Process.Output.Stopped -> {
                                    println("background runtime stopped")
                                    false
                                }
                                is Process.Output.StoppedWithError -> {
                                    println("background flow stopped with error")
                                    true
                                }
                            }
                        }
                    }
                    false -> scope.parallel {false}
                } }
            }.toFlow<Boolean,Boolean>(
                conditions {
                    testObject(true)
                    check{b->b}
                    updateCondition { b -> b }
                }
            )
        }

        val c = Component()
       // delay(1_000)
        val bool = c.flow.evolve(true).get()
        assert(!bool)
        delay(100)
        assert(c.outputStack.isEmpty())

    }

}