package org.drx.evoleq.math

import org.drx.evoleq.dsl.Configuration
import org.junit.Test

class ConfigurationMathTest {
    @Test
    fun testInject() {
        open class Ex(val x :Int )
        open class ExConf : Configuration<Ex> {

            var x: Int = 0

            override fun configure(): Ex = Ex(x)
        }
        class Ex1(z:Int = 0, val y: Int): Ex(z)
        class Ex1Conf : ExConf() {


            var y: Int = 0

            override fun configure(): Ex1 = Ex1(x,y)
        }
        val f0 : ExConf.()->Unit = {
            x = 1
        }

        val f1 : Ex1Conf.()->Unit = {
            //x = 1
            y = 2
        }
        val c1 = Ex1Conf()
        c1.f0()
        c1.f1()
        val c = c1.configure()
        assert(c.x == 1)

        val f2 : ExConf.()->Unit = {
            x = 3
        }
        c1.f2()
        val cs: Ex1Conf.(ExConf.()->Unit)->Ex1Conf = {f ->
            this.f()
            this
        }


        val d = c1.cs(f0).configure()
        assert(d.y == 2)

        val cc = Ex1Conf()
        val cc1 = consumeConfig<ExConf,Ex1Conf>(cc){
            x = 3
        }
    }
}