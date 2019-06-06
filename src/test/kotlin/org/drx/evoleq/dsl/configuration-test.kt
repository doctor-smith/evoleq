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
package org.drx.evoleq.dsl

import kotlinx.coroutines.runBlocking
import org.junit.Test

class ConfigurationTest {
    @Test
    fun testConfiguration() = runBlocking{
        class Ex(val x: Int, val  y: String)
        class ExConfig(var x : Int? = null, var y: String? = null) : Configuration<Ex> {
            override fun configure(): Ex {
                return Ex(x!!, y!!)
            }
        }
        val ex = configure<Ex, ExConfig> {
            x = 1
            val c = "trala"
            y = "supi"
            x = 2
        }
        //delay(100)
        assert(ex.x == 2)
        assert(ex.y == "supi")
    }
    @Test
    fun testConfigurationOnVararg() {
        class Ex(vararg items: Int){
            val items =  items
        }
        class ExConfig(vararg items:Int = IntArray(0)) : Configuration<Ex> {
            var items = items
            fun items(vararg items: Int) {this.items = intArrayOf(*items)}
            override fun configure(): Ex {
                return  Ex(items = *items)
            }
        }
        val ex = configure<Ex, ExConfig> {
            items(
                1,2,3
            )
        }
        assert(ex.items.size == 3)
    }

    @Test
    fun inheritance() {
        class Ex<out T>(val x: T)
        class ExConfig(var x : Int? = null) : Configuration<Ex<Any>> {
            override fun configure(): Ex<Any> {
                return Ex(1)
            }
        }

    }

    @Test
    fun configureConfiguration() {
        class Ex<out T>(val x: T)
        class ExConfig(var x : Int? = null) : Configuration<Ex<Any>> {
            override fun configure(): Ex<Any> {
                return Ex(1)
            }
        }
        val c = setupConfiguration<Ex<*>,ExConfig>{
            x=2
        }
    }



    @Test fun configureWithConstructor() {
        class Ex<out T>(val x: T)
        class ExConfig(var x : Int) : Configuration<Ex<Int>> {
            override fun configure(): Ex<Int> {
                return Ex(x)
            }
        }
        val c = configure<Ex<Int>,ExConfig, Int>(2){x+=2}
        assert(c.x == 4)
    }
    @Test fun configureWithConstructor2() {
        class Ex<out T>(val x: T, val y: T)
        class ExConfig(var x : Int, var y: Int) : Configuration<Ex<Int>> {
            override fun configure(): Ex<Int> {
                return Ex(x,y)
            }
        }
        val c = configure<Ex<Int>,ExConfig>(constructor(1,1)){
            x+=1
            y+=1
        }
        assert(c.x+c.y == 4)
    }
}