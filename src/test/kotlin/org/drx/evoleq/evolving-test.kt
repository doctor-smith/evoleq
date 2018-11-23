package org.drx.evoleq

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.Exception

class EvolvingTest {

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

}