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

import javafx.beans.property.SimpleIntegerProperty
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.immediate
import org.drx.evoleq.dsl.lazyImmediate
import org.drx.evoleq.dsl.parallel
import org.junit.Test
import java.lang.Thread.sleep

class ImmediateTest{
    @Test fun sequentialInitialization() = runBlocking{
        val prop = SimpleIntegerProperty(0)
        prop.addListener { observable, oldValue, newValue ->
            println("assertion! ")
            assert(newValue.toInt() > oldValue.toInt())
        }
        val one = Immediate{
            sleep(1000)
            println(1)
            prop.value = 1
            1
        }
        val two = Immediate{
            println(2)
            prop.value = 2
            2
        }
        //one.get()
        delay(1_500)
        Unit
    }


    @Test fun immediateBlocksConveniently() = runBlocking {
        val startTime = System.currentTimeMillis()
        val parallel = CoroutineScope(Job()).parallel {
            immediate { sleep(10_000) }
            println("fhdkjlsa")
        }
        assert(System.currentTimeMillis()-startTime < 1_000)

        parallel.job.cancel()
    }

    @Test fun cancelImmediate() = runBlocking {
        val scope = DefaultEvolvingScope()
        var job: Job? = null
        scope.parallel{
            immediate( 0){
                job = this.coroutineContext[Job]
                sleep(10_000)
                1
            }
        }
        delay(100)
        scope.cancel()
        delay(100)
        assert(job!!.isCancelled)
    }

    @Test fun lazyImmediateT() = runBlocking {
        val lI: LazyImmediate<Int> = lazyImmediate<Int>{ x  -> x*x }

        Unit
    }

    @Test fun cancellation() = runBlocking{
        val imm = Immediate{
            sleep(100)
            Unit
        }
        val cancelled = imm.cancel(Unit)
        println(cancelled.job)
        delay(100)
        assert(cancelled.job.isCompleted)
    }
}