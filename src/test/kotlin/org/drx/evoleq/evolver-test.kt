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

import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.experimental.flow.ChattyFlowBase
import org.drx.evoleq.experimental.flow.OneWayPipe
import org.drx.evoleq.experimental.flow.TwoWayFlangedData
import org.drx.evoleq.experimental.flow.TwoWayPipe
import org.drx.evoleq.time.WaitForProperty
import org.junit.Test

class EvolverTest {
    @Test
    fun testChattyFlowBase() = runBlocking {

        val pipe = TwoWayPipe<Int, Int>(
            OneWayPipe(),
            OneWayPipe()
        )
        pipe.io().input(0)

        val conditions =
            EvolutionConditions<TwoWayFlangedData<Int, Int, String>, TwoWayFlangedData<Int, Int, String>>(
                testObject = TwoWayFlangedData(pipe.oi(), "WAIT"),
                check = { twoWayFlangedData -> twoWayFlangedData.flange.output().value <= 5 },
                updateCondition = { d -> d }
            )

        val flow: (TwoWayFlangedData<Int, Int, String>) -> Evolving<TwoWayFlangedData<Int, Int, String>> = {
            data ->
            Parallel {
                println("huhuhuhuhu")
                data.flange.input((data.flange.output().value + 1))
                val newData = data.copy(data = "RUNNING")
                WaitForProperty(data.flange.output()).toChange()
                newData
            }
        }
        pipe.io().input(0)
        assert(pipe.oi().output().value == 0)
        pipe.io().output().addListener{_,_,nV ->
            println("hahahahaha: $nV")
            //if(nV >= 10) {
                Thread.sleep(1_000)
                pipe.io().input(nV)
            //}
        }

        val chattyFlow = ChattyFlowBase(
            chattyConditions = conditions,
            chattyFlow = flow,
            pipe = pipe
        )
        //runBlocking {
            val data = chattyFlow.evolve(TwoWayFlangedData(pipe.oi(), "GO"))

            assert(data.get().flange.output().value == 6)
        //}
    }
}