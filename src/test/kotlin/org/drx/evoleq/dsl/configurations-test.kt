package org.drx.evoleq.dsl

import org.junit.Test

class ConfigurationsTest {
    @Test
    fun testIt() {
        val configurations = Configurations()

        class Ex(val x: Int, val  y: String)
        class ExConfig(var x : Int? = null, var y: String? = null) : Configuration<Ex> {
            override fun configure(): Ex {
                return Ex(x!!, y!!)
            }
        }
        class Impl1
        class Impl2
        class Impl3


        configurations.register<Impl1,ExConfig>(ExConfig(1,"1"))
        configurations.register<Impl2,ExConfig>(ExConfig(2,"2"))
        configurations.register<Impl3,ExConfig>(ExConfig())
        val c1 = (configurations.get<Impl1>() as ExConfig).configure()
        assert (c1.x == 1 && c1.y == "1")

        val c2 = (configurations.get<Impl2>() as ExConfig).configure()
        assert (c2.x == 2 && c2.y == "2")

        val cc3 = (configurations.get<Impl3>() as ExConfig)
        cc3.x = 3
        cc3.y = "3"

        val c3 = cc3.configure()

        class Impl4
        configurations.register<Impl4, ExConfig>(setupConfiguration<Ex,ExConfig> { x = 3 } as ExConfig)

    }
}