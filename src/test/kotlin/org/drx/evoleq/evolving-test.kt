package org.drx.evoleq

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import java.lang.Exception

class EvolvingTest {

    @Test
    fun testTimes() {
        val f = {s:String->Parallel {
            delay(1_000)
            s.length
        }}
        val g = {i:Int -> Parallel {
            i.toDouble()
        }}
        runBlocking {
            val h = Parallel { f * g }.get()
            val j = f*g
            assert(h("kind").get() == 4.0)
            assert(j("kind").get() == 4.0)
        }
        var x =2.0
        GlobalScope.launch { (f*g)("1") }.invokeOnCompletion {  }
    }


    @Test
    fun testRepetition() {
        runBlocking {
            val f = {s: Int -> s + 1}
            val x = repeatImmediate(10, 0, f)
            assert(x == 10)
        }
    }

    @Test
    fun testBackPropagationFirstAttempt() {

        val stub = object{
            fun get(): Int = 3
        }

        runBlocking {
            /* TODO implement */
            val flow = Flow(
                once()
            ){
                pair:Pair<Int,(Int)->Int> -> Immediate{

                    val x = repeatImmediate(5,pair.first,pair.second)

                    Pair(x,pair.second)
                }
            }

            assert(flow.evolve(Pair(0, {s:Int->s+1})).get().first == 5)
            assert (stub.get() == 3)
        }
    }

    @Test
    fun testBackPropagationSndAttempt() = runBlocking {

        data class P(val message: String)
        data class W(val p: P)

        val from = { w: W -> Immediate { w.p } }
        val to = { w: W -> { p: P -> Immediate { w.copy(p = p) } } }
        val gap: Gap<W, P> = Gap(from, to)

        class SideEffect(gap: Gap<W, P>) : Spatula<W, P> {
            val fProp =  SimpleObjectProperty<(W)->Evolving<W>>()
            var fSet = false
            init{Parallel{
                println("init")
                fProp.value = fill(gap)
                fSet = true
            }}
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
                while(!fSet){
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