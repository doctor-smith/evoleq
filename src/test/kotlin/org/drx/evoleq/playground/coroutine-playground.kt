/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.playground

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CoroutinePlayground {
    @Test
    fun exit() = runBlocking{
        val x = {

            var z = 0
            GlobalScope.launch exit@{
                z = 5
                return@exit
            }
            println(z)
        }
    }
    @Test
    fun exitFun() {
        exit@ fun<T,D> T.exit(block:T.()->D): D = this.block()
        var y = exit@{
            val N = 3.0
            return@exit N
            Unit


        }
    }
}