package org.drx.evoleq.experimental

import kotlinx.coroutines.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.data.Evolving
import org.drx.evoleq.data.Parallel
import org.drx.evoleq.evolve
import org.junit.Test

typealias Data = Pair<Int,Int>

class EvolEqTest{
    @Test
    fun test1()
    {
        val N = 10
        val data = Data(0, 0)
        val conditions = EvolutionConditions<Data, Int>(
            testObject = 0,
            check = { x -> x <= N },
            updateCondition = { newData -> newData.second }
        )
        val f: (Data) -> Evolving<Data> = { oldData ->
            Parallel {
                Thread.sleep(100)
                val newData = Data(oldData.first + oldData.second, oldData.second + 1)
                //println(newData.first)
                newData
            }
        }
        GlobalScope.async {
            val newData = evolve(data, conditions) { data ->
                f(data)
            }
            assert(newData.first == N*(N + 1)/2)
        }

        Thread.sleep(1_000)
    }


}