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
import org.drx.evoleq.dsl.onDemand
import org.drx.evoleq.dsl.parallel
import org.junit.Test

class OnDemandTest {

    @Test fun computeResultOnDemand() = runBlocking {

        var computationDone = false

        val evolving = onDemand {
            delay(1_000)
            computationDone = true
            5
        }
        delay(500)
        assert(evolving.job.isActive)
        val time = System.currentTimeMillis()
        parallel{
            delay(1000)
            val result = evolving.get()
        }
        delay(1900)
        assert(evolving.job.isActive)
        while(!computationDone) {
            delay(1)
        }
        assert(System.currentTimeMillis() - time >= 2000)
        delay (100)
        assert(evolving.job.isCompleted)

    }
}