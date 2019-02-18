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
package org.drx.evoleq.experimental.evolving

import kotlinx.coroutines.*
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.junit.Test
import java.lang.Thread.sleep

class ParallelCancelableTest {
    @Test
    fun parallelCancelable() = runBlocking {
        var job: Job? = null
        val x = ParallelCancelable(scope = this) {
                job = launch{

                    delay(1500)
                }

                println("ok")
                delay(1_000)
                Parallel<Unit>(scope = this){
                    Immediate{delay(200)}

                }
                100

        }

        var y: Int = 0
        Parallel<Int> {
            y = x.get()
            y!!
        }
        delay(500)
        x.cancel(10)
        delay(100)
        //x.coroutineContext.cancel()
        println(y)
        assert(y == 10)

        val time = System.currentTimeMillis()
        while(job!!.isActive){
            delay(1)
        }
        println(System.currentTimeMillis() - time)
        //assert(job!!.isCancelled)
        assert(job!!.isCompleted)


    }

}