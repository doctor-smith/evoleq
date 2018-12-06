package org.drx.evoleq.experimental

import kotlinx.coroutines.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.experimental.flow.ChattyFlow
import org.drx.evoleq.time.WaitForProperty

class ChattyFlowTest {
    //@Test
    fun testIt()  {
        val chattyFlow = object: ChattyFlow<String, Boolean, Int, String>(
            conditions = EvolutionConditions(
                testObject = true,
                check = { b -> b },
                updateCondition = { s -> s.length < 100_000 }
            ),
            flow = {s -> Immediate { delay(1);s + s } } ,
            initialMessage = 0
        ) {
            override fun chattyCheck(t: Pair<Int, String>): Boolean {
                return t.first <100 && conditions.updateCondition(t.second)
            }

            override fun chattyUpdate(p: Pair<Int, String>): String {
                pipe.oi().input(p.second)
                return "${p.first}"
            }

            override fun waitForInput()  {
                Parallel { WaitForProperty(output()).toChange() }
            }
        }
        var d = "";
        runBlocking {
            GlobalScope.launch { coroutineScope{ d = chattyFlow.evolve("s ").get() }}
            delay(100)
            chattyFlow.input(99)
            delay(10)
            chattyFlow.input(99)
            delay(10)
            chattyFlow.input(99)
            delay(10)
            chattyFlow.input(101)
            assert(d ==   "")
        }
        println(d.length)


        //Thread.sleep(10_000)
    }
}