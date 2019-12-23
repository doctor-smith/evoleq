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
package org.drx.evoleq.util

import org.junit.Test

class PropertiesTest{

    @Test fun negation() {
        val property = booleanProperty(true)
        val negated = !property

        property.value = true
        assert(negated.value == false)

    }

    @Test fun and() {
        val property1 = booleanProperty()
        val property2 = booleanProperty()
        val and = property1 and property2

        assert(!and.value)

        property1.value = true
        assert(!and.value)
        property2.value = false
        assert(!and.value)
        property2.value = true
        assert(and.value)
    }

    @Test fun or() {
        val property1 = booleanProperty()
        val property2 = booleanProperty()
        val or = property1 or property2

        assert(!or.value)

        property1.value = true
        assert(or.value)
        property2.value = false
        assert(or.value)
        property2.value = true
        assert(or.value)
    }

    @Test fun xor() {
        val property1 = booleanProperty(false)
        val property2 = booleanProperty(false)
        val xor = property1 xor property2

        assert(!xor.value)

        property1.value = true
        assert(xor.value)
        property2.value = true
        assert(!xor.value)
        property1.value = false
        assert(xor.value)
    }
}