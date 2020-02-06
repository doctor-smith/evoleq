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
package org.drx.evoleq.evolving

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor
import org.drx.evoleq.dsl.lazyParallel
import org.drx.evoleq.dsl.onScope
import org.drx.evoleq.dsl.parallel
import org.junit.Test

class ParallelTest {
    @Test
    fun performance() = runBlocking {
        var time = System.currentTimeMillis()
        val parallel = Parallel<Int>{
            1
        }
        println(System.currentTimeMillis()-time)
        time = System.currentTimeMillis()
        val x = parallel.get()
        time = System.currentTimeMillis() - time
        println(time)
        time = System.currentTimeMillis()
        var time1 = System.currentTimeMillis()
        val parallel1 = Parallel<Int>{
            delay(1_000)
            1
        }
        time = System.currentTimeMillis() - time
        println(time)
        parallel1.cancel(2)
        time1 = System.currentTimeMillis() -time1
        println(time1)


    }

    @Test fun isParallelExecution() = runBlocking {
        var launchingTime = System.currentTimeMillis()
        var execTime = System.currentTimeMillis()
        val parallel = Parallel<Unit>{

            delay(1_000)
            Unit
        }
        launchingTime = System.currentTimeMillis() - launchingTime
        assert(launchingTime <= 100)
        val x = parallel.get()
        execTime = System.currentTimeMillis() - execTime
        assert(execTime >= 1_000)
    }

    @Test fun isParallel() = runBlocking{
        val parallel1 = Parallel<Int>(scope = this){
            var i = 0
            while(i < 10) {
                delay(10)
                println("running 1")
                i++
            }

            1
        }
        val parallel2 = Parallel<Int>(scope = this){
            var i = 0

                while (i < 10) {
                    delay(10)
                    println("running 2")
                    i++
                }


            2
        }
        val x2 = parallel2.get()
        val x1 = parallel1.get()
    }

    @Test fun readTwice() = runBlocking{
        val parallel = Parallel<Int>{
            kotlinx.coroutines.delay(20)
            1
        }
        val x = parallel.get()
        val y = parallel.get()

        assert(x == y)
    }

    @Test fun cancellation() = runBlocking {
        var inner: Parallel<Int>? = null
        val parallel = parallel<String>(){
            inner = parallel{
                delay(10_000)
                1
            }// as Parallel<Int>
            delay(5000)
            "parallel"
        }

        while(inner == null){
            delay(100)
        }
        //delay(1_000)


        val x = parallel.cancel("parallel_cancelled")
        val y =parallel.get()
        //assert(x==y)
        assert(parallel.job.isCancelled)
        assert(inner!!.job.isCancelled)
        assert(x.get() == "parallel_cancelled")
        Unit
    }


    @Test fun scope() = runBlocking {
        val x = parallel{
            actor<Int> {

            }
        }
        Unit
    }



    @Test fun lazyParallelTest() =runBlocking {
        val x: LazyParallel<Int> = lazyParallel{d->
            delay(10_000)
            d*d
        }

        val s1 = CoroutineScope(Job())
        val r1 = s1.x(5)

        s1.cancel()
        delay(1_000)
        assert(r1.job.isCancelled)
        assert(r1.get() == 5)

        val y: LazyParallel<Int> = lazyParallel{d->
            d*d
        }

        val s2 = CoroutineScope(Job())
        val r2 = s2.y(10)
        val r = r2.get()
        assert(r == 100)

    }

    @Test fun evolvingOnScope() = runBlocking {
        val ev = CoroutineScope(Job()).parallel(1,1) {
            delay(10_000)
            2
        }

        val evS = ev.onScope(GlobalScope)

        assert(ev.job.isActive)
        assert(evS.job.isActive)

        evS.job.cancel()

        delay(100)
        assert(evS.job.isCancelled)
        assert(ev.job.isCancelled)
        //println(ev.job.isActive)

        //println(ev.job.isCancelled)

        val r = ev.get()
        assert(r == 1)

        val rS = evS.get()
        assert(rS == 1)
    }

    @Test fun evolvingOnScope2() = runBlocking {
        val scope = CoroutineScope(Job())
        val ev = scope.parallel(1,1) {
            delay(10_000)
            2
        }

        val evS = ev.onScope(GlobalScope)

        assert(ev.job.isActive)
        assert(evS.job.isActive)

        scope.cancel()

        delay(100)
        assert(evS.job.isCancelled)
        assert(ev.job.isCancelled)
        //println(ev.job.isActive)

        //println(ev.job.isCancelled)

        val r = ev.get()
        assert(r == 1)

        val rS = evS.get()
        assert(rS == 1)
    }


    @Test fun lazyParallelToParallelRemindsScope() = runBlocking{
        val x: LazyParallel<Int> = lazyParallel{d->
            delay(10_000)
            d*d
        }

        val s1 = CoroutineScope(Job())

        val y: suspend (Int) -> Parallel<Int> = {t: Int -> x(s1,t)}

        val res = y(1)

        s1.cancel()

        assert(res.job.isCancelled)
    }
}