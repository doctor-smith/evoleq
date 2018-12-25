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
package org.drx.evoleq.gap

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.math.times

/**
 *
 * Usually P will be a part of W, so that we can get p via w.p and set it via w.p  = p.
 * In this case we can use
 *  - from: (W)-> Evolving<P> = {w -> Immediate{ w.p }}
 *  - to: (Pair<W,P>)->Evolving<W> = {pair -> Immediate{ pair.first.copy(w.p = p) }}
 */
data class Gap<W,P>(
    val from:(W)-> Evolving<P>,
    val to: (W)->(P)-> Evolving<W>
)

suspend fun <W,P,Q> Gap<W, P>.deepen(gap: Gap<P, Q>): Gap<W, Q> {
    val newFrom = from*gap.from
    val newTo= {w:W->
        {q:Q ->
            Immediate {
                (gap.to(from(w).get()) * to(w))(q).get()
            }
        }
    }
    return Gap(newFrom, newTo)
}

suspend fun <W,P,Q> Gap<P, Q>.widen(gap: Gap<W, P>): Gap<W, Q> {
    val newFrom = gap.from*from
    val newTo= {w:W->
        {q:Q ->
            Immediate {
                (to(gap.from(w).get()) * gap.to(w))(q).get()
            }
        }
    }
    return Gap(newFrom, newTo)
}

suspend fun <W, P> Gap<W, P>.fill(filler: (P)-> Evolving<P>): (W)-> Evolving<W> {
    return { w ->
        Immediate {
            val p = (from * filler)(w).get()
            to(w)(p).get()
        }
    }
}

suspend fun <W, P> Gap<W, P>.fill(filler: suspend (P)-> Evolving<P>): (W)-> Evolving<W> {
    return { w ->
        Immediate {
            val p = (from * filler)(w).get()
            to(w)(p).get()
        }
    }
}