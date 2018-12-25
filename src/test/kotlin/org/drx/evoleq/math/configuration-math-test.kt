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
package org.drx.evoleq.math

import org.drx.evoleq.dsl.Configuration
import org.junit.Test

class ConfigurationMathTest {
    @Test
    fun testConsumeConfig() {
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

        assert(cc1.x == 3)
        assert(cc1.y == 0)
    }
}