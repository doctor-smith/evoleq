package org.drx.evoleq

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.experimental.ChattyFlowBase
import org.drx.evoleq.experimental.OneWayPipe
import org.drx.evoleq.experimental.TwoWayFlangedData
import org.drx.evoleq.experimental.TwoWayPipe
import org.junit.Test

class EvolverTest {
    @Test
    fun testChattyFlowBase() = runBlocking {

        val pipe = TwoWayPipe<Int,Int>(OneWayPipe(), OneWayPipe())
        pipe.io().input(0)

        val conditions = EvolutionConditions<TwoWayFlangedData<Int, Int, String>,TwoWayFlangedData<Int,Int,String>>(
            testObject = TwoWayFlangedData(pipe.oi(),"WAIT"),
            check = {twoWayFlangedData -> twoWayFlangedData.flange.output().value <= 5  },
            updateCondition = {d -> d}
        )

        val flow: (TwoWayFlangedData<Int,Int,String>) -> Evolving<TwoWayFlangedData<Int,Int,String>> = {
            data -> Parallel {
                println("huhuhuhuhu")
                data.flange.input((data.flange.output().value +1))
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