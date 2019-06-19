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
import org.drx.evoleq.dsl.onScope
import org.drx.evoleq.dsl.parallel
import org.junit.Test

class ImmediateTest{
    @Test fun sequentialInitialization() = runBlocking{
        val prop = SimpleIntegerProperty(0)
        prop.addListener { observable, oldValue, newValue ->
            println("assertion! ")
            assert(newValue.toInt() > oldValue.toInt())
        }
        val one = Immediate{
            delay(1000)
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

        Unit
    }

    @Test
    fun immediateBlocksConveniently() = runBlocking {
        var ev: Evolving<Unit>? = null
        val startTime = System.currentTimeMillis()
        val parallel = CoroutineScope(Job()).parallel {
            ev = immediate { delay(10_000) }
            println("fhdkjlsa")
        }
        assert(System.currentTimeMillis()-startTime < 1_000)

        parallel.job.cancel()


        delay(1_000)
    }

    //@Test
    fun cancelImmediate() = runBlocking {
        var immediate: Immediate<Int>? = null
        val scope = DefaultEvolvingScope()

        scope.parallel{
            immediate = Immediate(this, 0){
                delay(10_000)
                1
            }
        }
        delay(100)
        scope.cancel()

        val res = immediate!!.get()
        assert(res == 0)
        assert(immediate!!.job.isCancelled)

    }
}