package org.drx.evoleq.math

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.DefaultEvolvingScope
import org.drx.evoleq.evolving.Parallel
import org.junit.Test

class ParallelMathTest {
    @Test fun mapParallel() = runBlocking {
        val scope = DefaultEvolvingScope()
        val startTime = System.currentTimeMillis()
        val parallel = scope.parallel{
            delay(10_000)
            1
        }

        val mapped = parallel.mapParallel { x -> "$x" }
        assert(System.currentTimeMillis() - startTime < 5_000)


        parallel.job.cancel()
        delay(100)
        assert(mapped.job.isCancelled)
    }

    @Test fun muParallel() = runBlocking{
        val scope = DefaultEvolvingScope()
        var innerParallel: Parallel<Int>? = null
        val parallel = scope.parallel {
            innerParallel = parallel{
                delay(1_000)
                1
            }
            innerParallel!!
        }
        delay(100)
        assert(innerParallel!!.job.isActive)
        val mu = muParallel(parallel)
        delay(100)
        assert(mu == innerParallel!!)


        mu.job.cancel()

        delay(200)

        //assert(parallel.job.isCompleted)

        assert(innerParallel!!.job.isCancelled)


    }

    @Test fun fishParallel() = runBlocking {
        val scope = DefaultEvolvingScope()

        val f = suspended {x:Int -> scope.parallel {
            delay(10_000)
            x*x
        }
        }
        val g = suspended {x: Int->scope.parallel {
            delay(1_000)
            "$x"
        }
        }
        val startTime = System.currentTimeMillis()

        val h = f fishParallel g

        assert(System.currentTimeMillis() - startTime < 5_000)

    }

    @Test fun fishParallelCancel() = runBlocking {
        val scope = DefaultEvolvingScope()

        var parallel1: Parallel<Int>? = null
        var parallel2: Parallel<String>? = null


        val f = suspended{x:Int ->
            parallel1 = scope.parallel {
                println("parallel_1")
                delay(2_000)
                x*x
            }
            parallel1!!
        }
        val g = suspended{x: Int->
            parallel2 = scope.parallel {
                println("parallel_2")
                delay(2_000)
                "$x"
            }
            parallel2!!
        }


        val h = f fishParallel g
        delay(100)
        h(2).job.cancel()
        //scope.cancel()
        delay(100)
        assert(parallel1!!.job.isCancelled)
        //assert(parallel2!!.job.isCancelled)

    }

}