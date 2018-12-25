/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.experimental

import org.junit.Test


class RecExperiments {
    @Test
    fun rexExp() {
        fun <A, C> rec(
            a: A,
            tO: C,
            ok: (C) -> Boolean,
            u: (A) -> C,
            f: (A) -> A
        ): A =
            when (ok(tO)) {
                true -> {
                    val na = f(a)
                    val ntO = u(na)
                    rec(
                        na,
                        ntO,
                        ok,
                        u,
                        f
                    )
                }
                false -> a
            }

        val x = rec(0, 1, ok = { x -> x < 20 }, u = { x -> x }) { x ->
            x + x + 1
        }
        assert(31 == x)
        println(x)


        val y = rec(a = Pair(0, 0), tO = 0, ok = { tO -> tO < 10 }, u = { p -> p.second }) {

                a ->
            Pair(a.first + a.second, a.second + 1)

        }
        println(y)
    }
}