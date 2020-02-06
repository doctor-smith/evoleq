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

import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.dsl.smartArrayListOf
import org.junit.Test

class BlockUntilTest {

    @Test fun blockUntil() = runBlocking{
        val property = SimpleStringProperty("")
        val delay = 1_000L
        parallel{
            delay(delay)
            property.value = "Cool"
        }
        var time = System.currentTimeMillis()
        blockUntil(property) { s -> s == "Cool"}
        time = System.currentTimeMillis() - time
        assert(time > delay)
    }

    @Test fun blockWhileEmpty() = runBlocking {
        val list = smartArrayListOf<String>()
        parallel{
            delay(1_000)
            list.add("NEXT")
        }
        var x :Int = 0
        //list.blockWhileEmpty()

        parallel {
            list.blockWhileEmpty()
            x = 1
        }
        delay(2_500)
        assert(x ==1)


    }

}