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
package org.drx.evoleq.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.evolving.Parallel
import org.junit.Test

class WaitTest {
    @Test fun later() = runBlocking {
        val later = Later<Int>()
        val ev = later.await()
        Parallel<Unit> {
            delay(10)
            later.value = 1
            later.value = 2
            assert(ev.get() == 1)
        }.get()
    }

    @Test fun change() = runBlocking{
        val change = Change(0)
        val ev = change.happen()
        Parallel<Unit> {
            //assert(ev.get() == 0)
            delay(10)
            change.value = 1
            assert(ev.get() == 1)
        }.get()
    }
}