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

    @Test fun cancelFished1 () = runBlocking {
        val scope = DefaultEvolvingScope()
        var job: Job? = null
        val f: suspend (String)-> Evolving<Int> = {s -> scope.parallel {
            job = this.coroutineContext[Job]
            delay(10_000)
            s.length
        }}
        val g: suspend (Int)->Evolving<Double> = {x -> scope.parallel { x.toDouble() }}

        val h = f * g

        val r = h("12")
        delay(100)
        scope.cancel()
        delay(100)

        //assert(job!!.isCancelled)
        r.job.isCancelled
        Unit
    }
}