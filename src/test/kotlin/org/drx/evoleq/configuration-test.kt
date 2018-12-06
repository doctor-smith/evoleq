package org.drx.evoleq

import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.configure
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
}