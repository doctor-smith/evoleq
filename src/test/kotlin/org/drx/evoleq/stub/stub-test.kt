/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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

import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.evolving.Immediate
import org.junit.Test

class StubTest {

    @Test
    fun stubWithParent() {

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
                        val parentStub = parent() as Stub<Int>
                        val x = parentStub.evolve(0)
                        val res = x.get().toString()
                        res
                        //"supi"
                    }}
                },
                GetStateKey::class
            )

        }
        runBlocking {
            val flow = stub.evolve(1)
            println(flow.get())

        }
    }

}