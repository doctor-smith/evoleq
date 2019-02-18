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
package org.drx.evoleq.gap

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.flow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.flow.Flow
import org.junit.Test

class SpatulaTest {

    @Test
    fun testSpatulaSimpleSideEffect() = runBlocking{

        data class P(val message: String)
        data class W(val p: P)

        val from = { w: W -> Immediate { w.p } }
        val to = { w: W -> { p: P -> Immediate { w.copy(p = p) } } }
        val gap: Gap<W, P> = Gap(from, to)

        class SideEffect(gap: Gap<W, P>) : Spatula<W, P> {
            val fProp =  SimpleObjectProperty<(W)-> Evolving<W>>()
            var fSet = false
            init{
                Parallel<Unit> {
                    println("init")
                    fProp.value = fill(gap)
                    fSet = true
                }
            }
            override suspend fun fill(gap: Gap<W, P>): (W) -> Evolving<W> {
                val filler: (P) -> Evolving<P> = { p ->
                    Parallel {
                        println("Wait ...")
                        delay(1000)
                        println("Message is: ${p.message}")


                        P("Ok")
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

        val se = SideEffect(gap)
        val p = se.print(W(P("nice message"))).get().p
        println(p.message)
    }


    @Test
    fun callSpatula() {

        class Data(val f: ((Int) -> Evolving<Int>)? = null, val gap: Gap<Int,String>)
        open class FillGap<W,P>
        class FillGapRequest<W,P>(val gap: Gap<W,P>) : FillGap<W,P>()
        class FillGapResponse<W,P>(val f: (W) -> Evolving<W>)  : FillGap<W,P>()
        class SideEffect : Spatulas<String> {
            override suspend fun <W> spatula(w: W): Spatula<W, String> {
                return object: Spatula<W,String> {
                    override suspend fun fill(gap: Gap<W, String>): (W) -> Evolving<W> {
                        return {w -> Immediate{w}}
                    }
                }
            }
        }

        val connect = flow<FillGap<Int,String>,Boolean>{
            conditions(once())
            flow{
                fillGap -> when(fillGap) {
                    is FillGapRequest<*,*> -> Immediate{FillGapResponse<Int,String>(
                        SideEffect().spatula(0).fill(fillGap.gap as Gap<Int,String>)
                    )}
                    is FillGapResponse<*,*> -> Immediate{fillGap}
                    else -> Immediate{fillGap}
                }
            }
        }
        fun <W,P> connect(spatula: Spatula<W,P>): Flow<FillGap<W,P>,Boolean> = flow{
            conditions(once())
            flow{
                when (it) {
                    is FillGapRequest<W,P> -> Immediate{
                        FillGapResponse<W,P>(
                            spatula.fill(it.gap)
                        )
                    }
                    else -> Immediate{it}
                }
            }
        }
    }

}