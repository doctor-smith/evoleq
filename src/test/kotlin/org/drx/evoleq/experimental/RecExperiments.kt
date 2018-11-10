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