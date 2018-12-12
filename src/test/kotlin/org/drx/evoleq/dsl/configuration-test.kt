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
}