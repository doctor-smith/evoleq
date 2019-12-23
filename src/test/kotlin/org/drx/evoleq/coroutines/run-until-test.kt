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

import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleStringProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.util.IntProperty
import org.drx.evoleq.util.booleanProperty
import org.drx.evoleq.util.intProperty
import org.junit.Test

class RunUntilTest {

    @Test fun runUntil() = runBlocking {
        val condition: SimpleStringProperty = SimpleStringProperty()
        parallel{
            delay(1_000)
            condition.value = "SET"
        }
        var time = System.currentTimeMillis()
        val result = runUntil(condition, {s -> s == "SET"}){
            delay(10_000)
        }
        time = System.currentTimeMillis() - time
        assert(time < 1_100)
        assert(result == null)
        condition.set("")
        parallel{
            delay(1_000)
            condition.value = "SET"
        }

        var result1 = runUntil(condition, {s -> s == "SET"}){
            Unit
        }
        assert(result1 == Unit)
    }

    @Test fun runUntilDecomposedFunctions () = runBlocking {
        val condition : IntProperty = intProperty(0)
        parallel{
            delay(500)
            condition.value = 1
        }
        val result = run {delay(1000)} until (condition fulfills { x: Int -> x > 0})
        assert(result == null)
    }

    @Test fun runAsLongAsDecomposedFunctions () = runBlocking {
        val condition: IntProperty = intProperty(0)
        parallel {
            delay(500)
            condition.value = 1
        }
        val result = run { delay(1000) } asLongAs (condition fulfills { x: Int -> x <= 0 })
        assert(result == null)
    }

    @Test fun isTrue() = runBlocking {
        val condition: BooleanProperty = booleanProperty(false)
        parallel {
            delay(500)
            condition.value = true
        }
        val result = run { delay(1000) } until condition.isTrue()
        assert(result == null)

        parallel {
            delay(500)
            condition.value = false
        }
        val result1 = run { delay(1000) } asLongAs condition.isTrue()
        assert(result1 == null)
    }

    @Test fun isFalse() = runBlocking {
        val condition: BooleanProperty = booleanProperty(false)
        parallel {
            delay(500)
            condition.value = true
        }
        val result = run { delay(1000) } asLongAs  condition.isFalse()
        assert(result == null)

        parallel {
            delay(500)
            condition.value = false
        }
        val result1 = run { delay(1000) } until condition.isFalse()
        assert(result1 == null)
    }
}