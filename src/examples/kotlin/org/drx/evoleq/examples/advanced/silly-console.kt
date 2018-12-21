/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.examples.advanced

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.Spatula
import org.drx.evoleq.gap.fill
import java.util.*

/**
 * Simple program that shows how to use Gap and Spatula to perform side-effects on a part P of a whole W.
 */

data class P(val message: String)
data class W(val p: P, val cnt: Int)

fun main(args: Array<String>) = runBlocking{

    val from = { w: W -> Immediate { w.p } }
    val to = { w: W -> { p: P -> Immediate { w.copy(p = p, cnt = w.cnt + 1) } } }
    val gap: Gap<W, P> = Gap(from, to)

    class SillyConsole(gap: Gap<W, P>) : Spatula<W, P> {
        val fProp =  SimpleObjectProperty<(W)-> Evolving<W>>()
        var fSet = false
        init{
            Parallel {
                fProp.value = fill(gap)
                fSet = true
            }
        }
        override suspend fun fill(gap: Gap<W, P>): (W) -> Evolving<W> {
            val filler: (P) -> Evolving<P> = { p ->
                Immediate {
                    val input = Scanner(System.`in`)
                    println(">${p.message}")
                    val answer = input.next()
                    P(answer)
                }
            }
            val f = gap.fill ( filler )
            return f
        }
        fun print(w:W): Evolving<W> = Parallel {
            while (!fSet) {
                delay(1)
            }
            val f = fProp.value
            f(w).get()
        }
    }

    val console = SillyConsole(gap)
    val sentence = "Write 'quit' to quit"

    evolve(
        initialData = W(P(sentence),0),
        conditions = EvolutionConditions(
            testObject = "",
            check = { s: String -> s != "quit" },
            updateCondition = { w: W -> w.p.message }
        )
    ){
        w -> when(w.cnt){
            0,1,2,3,4,5,6,7,8,9 ->console.print(w.copy(p = P("${w.cnt}: $sentence")))
            else -> console.print(w.copy(p = P("${w.cnt}: Are you crazy? $sentence!")))
    }
    }
    System.exit(0)
}

