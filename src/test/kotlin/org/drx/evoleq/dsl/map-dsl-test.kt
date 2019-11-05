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
package org.drx.evoleq.dsl

import org.junit.Test

class MapDslTest {
    @Test fun testFrom() {
        class Class(val prop1: Int)
        val c = Class(7)
        val map = map<String,Int>{
            "x" to 1
            from(c) {
                "number" to prop1
            }
        }

        val x = map["x"]!!
        val number = map["number"]!!
        assert(x == 1)
        assert(number == 7)
    }
}