package org.drx.evoleq.math

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.immediate
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.drx.evoleq.evolving.Evolving
import org.junit.Test

class EvolvingMathTest {

    @Test fun mapSuspended() = runBlocking{
        val scope = DefaultEvolvingScope()
        val ev = scope.parallel { 3 }

        val res = ev map suspended{ x: Int -> "$x"}

        assert(res.get() == "3")
    }


    @Test fun fishOperator1() = runBlocking{
        val scope = DefaultEvolvingScope()
        val f: (String)-> Evolving<Int> = {s -> scope.parallel { s.length }}
        val g: (Int)->Evolving<Double> = {x -> scope.immediate { x.toDouble() }}

        val h = f * g

        assert(h("12").get() == 2.0)
    }

    @Test fun fishOperator2() = runBlocking{
        val scope = DefaultEvolvingScope()
        val f: suspend (String)-> Evolving<Int> = {s -> scope.parallel { s.length }}
        val g: (Int)->Evolving<Double> = {x -> scope.immediate { x.toDouble() }}

        val h = f * g

        assert(h("12").get() == 2.0)
    }

    @Test fun fishOperator3() = runBlocking{
        val scope = DefaultEvolvingScope()
        val f: (String)-> Evolving<Int> = {s -> scope.parallel { s.length }}
        val g: suspend (Int)->Evolving<Double> = {x -> scope.immediate { x.toDouble() }}

        val h = f * g

        assert(h("12").get() == 2.0)
    }

    @Test fun fishOperator4() = runBlocking{
        val scope = DefaultEvolvingScope()
        val f: suspend (String)-> Evolving<Int> = {s -> scope.parallel { s.length }}
        val g: suspend (Int)->Evolving<Double> = {x -> scope.immediate { x.toDouble() }}

        val h = f * g

        assert(h("12").get() == 2.0)
    }

    //@Test TODO
    fun cancelFished1 () = runBlocking {
        val scope = DefaultEvolvingScope()
        var job: Job? = null
        var parallel1: Evolving<Int>? = null
        var parallel2: Evolving<Double>? = null
        val f: suspend (String)-> Evolving<Int> = {s ->
            parallel1 = scope.parallel {

            //delay(1_000)
            s.length
        }
            parallel1 as Evolving<Int>
        }
        val g: suspend (Int)->Evolving<Double> = {x ->
            parallel2 = scope.parallel {
                delay(2_000)
                x.toDouble()
            }
            parallel2 as Evolving<Double>
        }

        val h = f * g

        val r = h("12")
        delay(100)
        scope.cancel()
        delay(100)

        //assert(job!!.isCancelled)
        r.job.isCancelled
        assert(parallel2!!.job.isCancelled)
        Unit
    }
}