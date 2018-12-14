package org.drx.evoleq.dsl

import kotlin.reflect.full.createInstance

interface Configuration<out D>
{
    fun configure() : D
}
open class ConfigurationConfiguration<D> : Configuration<Configuration<D>> {

    var conf: Configuration<D>? = null

    override fun configure(): Configuration<D> = conf!!
}
inline fun <D, reified C: Configuration<D>> configure(noinline sideEffect:  C.()->Unit) : D {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configure()
}

inline fun <D, reified C: Configuration<D>> setupConfiguration(noinline sideEffect:  C.()->Unit) : C {
    val c = C::class.createInstance()
    c.sideEffect()
    return c
}

inline fun <reified C> C.reconfigure(block:C.()->C): C {
    return this.block()
}

