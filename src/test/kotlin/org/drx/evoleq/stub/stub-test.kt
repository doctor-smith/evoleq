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
package org.drx.evoleq.stub

import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.dsl.*
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.message.Message
import org.junit.Test
import java.lang.Thread.sleep

class StubTest {

    @Test
    fun stubWithParent() = runBlocking {

        class GetStateKey
        class ChildKey

        val stub = stub<Int>{

            val state = SimpleIntegerProperty(0)

            evolve{ x -> Immediate{
                state.value = x+1
                val child = child(ChildKey::class) as Stub<String>
                println(child.toFlow(once()).evolve("").get())
                state.get()
            } }

            parentalStub(GetStateKey::class, stub<Int>{
                evolve {x -> Immediate{state.get()}}
            })

            child(ChildKey::class,
                stub<String>{
                    evolve{ s -> Immediate {
                        val parentStub = parent<Int>()
                        val x = parentStub.evolve(0)
                        val res = x.get().toString()
                        res
                        //"supi"
                    }}
                },
                GetStateKey::class
            )

        }

        val flow = stub.evolve(1)
        assert(flow.get() == 2)
    }

    @Test
    fun observingStub() = runBlocking {
        val prop = SimpleStringProperty()//SimpleObjectProperty<String>()
        val change = SimpleObjectProperty<String>()
        val stub = observingStub<Int,String>{
            observe(prop)

            evolve { x -> Immediate{
                x
            }}
            gap{
                from{ x -> Immediate{ "$x" } }
                to{ x , y -> Immediate{
                    change.value = "$x"+y
                    ("$x"+y).length
                }}
            }
        }

            val x = stub.evolve(8)

            prop.value = "supi"

            val y = x.get()
            //sleep(1_000)
            assert(change.value == "8supi")
            assert(y == 5)

    }

    //@Test
    fun observingStubAsFlow() = runBlocking{
        class Data(val x: Int, val y: String, val clientId: Int)
        class Request(val message: String, val clientId: Int)
        val property = SimpleObjectProperty<Request>()
        val observingStub = observingStub<Data,Request> {
            gap{
                from {data -> Immediate{Request("waiting",data.x)}}
                to{data, request -> Immediate{
                    Data(data.x+1, request.message, request.clientId)}
                }
            }
            observe(property)
        }
        val callingStub = stub<Data>{
            evolve{ data -> observingStub.evolve(data) }
        }
        val flow = callingStub.toFlow<Data,Boolean>(
            conditions{
                testObject(true)
                check { b -> b }
                updateCondition { data: Data -> data.x < 700}
            }
        )

            delay(1_000)  // make sure that flow is configured before firing data
            val result  = flow.evolve(Data(0, "",-1))
            IntRange(1,11).forEach {i ->
                //GlobalScope.launch {
                Parallel<Unit>{
                    IntRange(1, 101).forEach {
                        property.value = Request("$it", i)
                        delay(1)
                    }
                }

            }
            assert(result.get().x >= 50)


    }

    @Test
    fun pauseObservingStub() =runBlocking {
        class Data(val x: Int, val cnt: Int = 0)
        class ObserverKey
        class OutKey
        class ChangeBehaviorKey
        class ObserveBehaviorKey
        val observe = SimpleObjectProperty<Boolean>(true)

        val observedProperty = SimpleObjectProperty<Int>()

        var launched = false

        val stub = stub<Data> {
            // make observer accessible to child processes
            // Clearly it is possible to access Observer directly via
            // fun observer() : child() ObserverKey::class
            // but then it is callable by all its siblings

            fun observer() = sibling(ChangeBehaviorKey::class,ObserverKey::class)!! as Stub<Int>


            evolve{ data -> Immediate{
                val observer = child(ObserverKey::class) as Stub<Int>
                    if(!launched) {
                        launched = true
                        val behavior = child(ObserveBehaviorKey::class) as Stub<Boolean>
                        Parallel<Unit>{val res =behavior.toFlow<Boolean,Boolean>(
                            conditions{
                                testObject(true)
                                check{b -> b}
                                updateCondition { b ->true }
                            }
                        ).evolve(true).get()
                        }

                    }
                    val res = observer.evolve(data.x).get()
                    Data(res, data.cnt+1)
            }}
            // parental stubs
            // prints received value to the console
            parentalStub(
                OutKey::class,
                stub<String>{
                    evolve{s-> Immediate{
                        println(s)
                        s
                    }}
                }
            )
            // Updates the observation behavior
            parentalStub(
                ChangeBehaviorKey::class,
                stub<Boolean> {
                    evolve{ b -> Immediate{
                        val observer = observer()//child(ObserverKey::class) as Stub<Int>
                        val obs = observer.stubs[ObservePropertyStub::class]!! as Stub<ObservePropertyMessage>
                        var o: Message? = null
                        if(b){
                            println("observe")
                            o =  obs.evolve(ObservePropertyMessage.Observe).get()
                        }
                        else{
                            println("pause")
                            o = obs.evolve(ObservePropertyMessage.Pause).get()
                        }
                        b
                    }}
                }
            )

            // children
            // Process observing the observed property
            child( observingStub<Int,Int>{
                    id ( ObserverKey::class )
                    gap{
                        from{ x -> Immediate{ x } }
                        to{x,y -> Immediate{
                            val parentStub = parent<String>()
                            val res = parentStub.evolve("received: $y").get()
                            y
                        }}
                    }
                    observe(observedProperty)
                },
                OutKey::class

            )

            // Process looking for changes of the observe property.
            child( ObserveBehaviorKey::class,
                observingStub<Boolean,Boolean>{
                    gap{
                        from{ x -> Immediate{ x } }
                        to{x,y -> Immediate{
                            val parentStub = parent<Boolean>()
                            val res = parentStub.evolve(y).get()
                            y
                        }}
                    }
                    observe(observe)
                },
                ChangeBehaviorKey::class
            )
            // siblings access
            ChangeBehaviorKey::class.grantAccessToSiblings(ObserverKey::class)
        }

        // Turn stub into flow
        val flow = stub.toFlow(
            conditions<Data, Boolean>{
                testObject(true)
                check{b -> b}
                updateCondition { data ->  data.cnt <= 2 }
            }
        )


            sleep(1_000)
            var done = false
            Parallel<Unit> {
                val result = flow.evolve(Data(0))
                val res = result.get().x
                assert (res == 4)
                println(res)
                done = true

            }
            observe.value = false
            delay(1_000)
            observedProperty.value = 1

            observe.value = true
            delay(1_000)
            observedProperty.value = 2
            observedProperty.value = 3
            observedProperty.value = 4
            observe.value = false
            observedProperty.value = 5
            while(!done){
                delay(10)
            }


    }
/*
    @Test
    fun racing() = runBlocking {
        class Data(val x : Int = 0, val data: String = "")
        open class Result(val value: String = "")

        class First
        class Second

        val wrapperStub = observingStub<Data, Result>{

            val property = SimpleObjectProperty<Result>()
            observe(property)


            gap{
                from{data -> Immediate{Result(data.data)}}
                to{data, result -> Immediate{Data(data.x,result.value)}}
            }
            child(First::class,
                stub<Result>{
                    evolve{  Parallel{
                        sleep(50)
                        val res = Result("first")
                        property.value = res
                        res
                    }
                    }
                }
            )
            child(Second::class,
                stub<Result>{
                    evolve{  Parallel{
                        sleep(10)
                        val res = Result("second")
                        property.value = res
                        res
                        }
                    }
                }
            )


            evolve{
                data -> Immediate{
                    val res1 = (child(First::class) as Stub<Result>).evolve(Result())
                    val res2 = (child(Second::class )as Stub<Result>).evolve(Result())
                    data
                }
            }
        }


        sleep(1_000) // make sure that configuration has taken place
        val res = wrapperStub.evolve(Data()).get()
        assert(res.data == "second")


    }
    */


    @Test
    fun racingStubTest() = runBlocking {

        val stub = racingStub<Int,Int> {
            timeout (1_000 )
            // drivers
            driver{ Immediate{
                delay(150)
                1
            }}
            driver{ Immediate{
                delay(100)
                2
            }}
            driver{ Immediate{
                delay(10)
                3
            }}
            // gap
            gap{
                from{ Immediate{ null } }
                to{x , y-> Immediate{
                    when(y==null){
                        true -> x
                        false ->x+y
                    }
                }}
            }
        }
        delay(1_000)
        val x = stub.evolve(0).get()
        //println(x)
        assert(x == 3)

    }

    @Test fun find() = runBlocking {
        class One
        class Two
        class Three
        var s : Stub<*>? = null
        val stub = stub<Unit> {
            id(One::class)
            child( stub<Unit>{
                id(Two::class)
                child(stub<Unit>{
                    id(Three::class)
                    evolve{Immediate{Unit}}
                })
                evolve{Immediate{Unit}}
            })
            evolve{
                Parallel{
                    //this@stub.whenReady{
                    s = this@stub.child(Two::class)
                    whenReady {
                        s = find(Three::class) as Stub<*>
                        println((s as Stub<*>).id)
                    }
                    assert(s != null)
                    Unit
                }
            }
            whenReady{
                s = find(Three::class) as Stub<*>
                assert(s != null)
                println("in whenReady" +(s as Stub<*>).id)
                println(stubs.size)
                stubs.forEach{
                    println(it.key)
                    println(it.value.stubs.size)
                }
            }

        }

        //delay(500)
        val d =  stub.evolve(Unit).get()
        assert(s != null)
        delay(1_000)
    }

    @Test fun receiverStubBasics() = runBlocking {

        val receiver: Receiver<Int> = receiver<Int>() {}

        val actorStub = receivingStub<Int, Int> {
            gap {
                from { x -> Immediate { x } }
                to { x, y -> Immediate { x + y } }
            }

            receiver(receiver)

            evolve { x ->
                Immediate {
                    //println(x)
                    x
                }
            }
        }

        val callingStub = stub<Int> {
            evolve { x ->
                actorStub.evolve(x)
            }
        }
        val N = 100
        val flow = callingStub.toFlow<Int, Boolean>(conditions {
            testObject(true)
            check { b -> b }
            updateCondition { x -> x <= N*(N+1) / 2 }
        })
        Parallel<Unit>{
            (1..(N+1)).forEach{Parallel<Unit>{receiver.send(it)}}

        }
        val res = flow.evolve(0)
        println(res.get())
        val response = (actorStub.stubs[ReceivingStub::class]!! as Stub<ReceivingStubMessage>).evolve(ReceivingStubMessage.Request.Close).get()

        assert(receiver.actor.isClosedForSend)
        //receiver.receiver.close()
        Unit
    }
}