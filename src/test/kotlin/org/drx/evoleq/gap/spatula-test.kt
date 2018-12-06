package org.drx.evoleq.gap

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
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
                Parallel {
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

}