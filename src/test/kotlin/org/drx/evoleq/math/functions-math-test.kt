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
package org.drx.evoleq.math

import org.junit.Test

class FunctionsMathTest {
    @Test
    fun testOverloading() {
        val f: (Unit)->Unit = {Unit}
        val g: suspend (Unit)->Unit = {Unit}
        val h = f then f
        val h1 = g then f
        val h2 = f then g
        val h3 = g then g
    }
}