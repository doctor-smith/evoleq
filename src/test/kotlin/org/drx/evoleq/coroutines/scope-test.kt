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
package org.drx.evoleq.coroutines

import kotlinx.coroutines.*
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Parallel
import org.junit.Test
import kotlin.coroutines.CoroutineContext

class ScopeTest {
    @Test fun scope() = runBlocking{
        val scoped = onScope { x: Int -> x * x }
        val r = parallel { scoped(2) }.get()
        assert(r == 4)
    }

    @Test fun cancel () = runBlocking{
        var j: Parallel<Int>? = null
        val f = onScope{
                scope: CoroutineScope, x: Int ->
                j = scope.parallel{
                delay(5_000)
                x+2
            }
            //j!!.get()
        }
        val job = GlobalScope.launch {
            f(this,3)
        }
        delay(100)
        assert(j!!.job.isActive)
        job.cancel()
        delay(100)
        assert(job.isCancelled)
        assert(j!!.job.isCancelled)
    }


}