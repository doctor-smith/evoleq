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

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.Thread.sleep

class RaceTest {

    @Test fun blockRace() = runBlocking{
        val x = blockRace(GlobalScope,{
            sleep(10)
            1
        },{
            sleep(1)
            2
        }).await()

        assert(x ==2)
    }

}