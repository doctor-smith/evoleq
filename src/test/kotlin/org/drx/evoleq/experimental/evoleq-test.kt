package org.drx.evoleq.experimental

import kotlinx.coroutines.*
import org.drx.evoleq.evolve
import org.junit.Test

typealias Data = Pair<Int,Int>

class EvolEqTest{
    @Test
    fun test1()
    {
        val data = Data(0,0)
        val testObject = 0
        val condition: (Int)->Boolean = { x -> x < 10 }
        val update: (Data)->Int = { newData -> newData.second }
        val f: (Data)-> Deferred<Data> = { oldData ->
            GlobalScope.async {
                Thread.sleep(100)
                val newData =Data(oldData.first + oldData.second, oldData.second +1)
                println(newData.first)
                newData
            }
        }
        GlobalScope.launch {
            val newData = evolve<Data, Int, Int>(data, testObject, condition, update, f)
        }
        Thread.sleep(1_000)
    }


}