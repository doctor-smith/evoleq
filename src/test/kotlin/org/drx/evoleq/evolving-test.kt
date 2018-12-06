package org.drx.evoleq

import kotlinx.coroutines.*
import org.drx.evoleq.conditions.once
import org.junit.Test

class EvolvingTest {

    @Test
    fun immediateDoesNotBreakParallel() = runBlocking{
        /* TODO find good test */
    }

    @Test
    fun testTimes() {
        val f = {s:String->Parallel {
            delay(1_000)
            s.length
        }}
        val g = {i:Int -> Parallel {
            i.toDouble()
        }}
        runBlocking {
            val h = Parallel { f * g }.get()
            val j = f*g
            assert(h("kind").get() == 4.0)
            assert(j("kind").get() == 4.0)
        }
        var x =2.0
        GlobalScope.launch { (f*g)("1") }.invokeOnCompletion {  }
    }


    @Test
    fun testRepetition() {
        runBlocking {
            val f = {s: Int -> s + 1}
            val x = repeatImmediate(10, 0, f)
            assert(x == 10)
        }
    }

    @Test
    fun testBackPropagationFirstAttempt() {

        val stub = object{
            fun get(): Int = 3
        }

        runBlocking {
            /* TODO implement */
            val flow = Flow(
                once()
            ){
                pair:Pair<Int,(Int)->Int> -> Immediate{

                    val x = repeatImmediate(5,pair.first,pair.second)

                    Pair(x,pair.second)
                }
            }

            assert(flow.evolve(Pair(0, {s:Int->s+1})).get().first == 5)
            assert (stub.get() == 3)
        }
    }

    @Test
    fun testBackPropagationSndAttempt() = runBlocking {

    }
}