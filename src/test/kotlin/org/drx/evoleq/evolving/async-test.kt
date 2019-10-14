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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AsyncTest {

    @Test
    fun isAsyncExecution() = runBlocking{
        val async1 = Async<String>(scope = this) {
            var i = 0
            while(i < 10) {
                kotlinx.coroutines.delay(100)
                println("running 1")
                i ++
            }
            "done 1"
        }
        val x1 = async1.get()
        val async2 = Async<String>(scope = this) {
            var i = 0
            while(i < 20) {
                kotlinx.coroutines.delay(50)
                println("running 2")
                i ++
            }
            "done 2"
        }

        val x2 = async2.get()
    }


    @Test fun cancel() = runBlocking {

        val async = Async<Int>{
            delay(1_000)
            1
        }

        val res = async.cancel(0)
        delay(10)
        assert(async.job.isCancelled)
        assert(res.get() == 0)
    }
}