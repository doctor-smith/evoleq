package org.drx.evoleq.dsl

import kotlin.reflect.full.createInstance

interface Configuration<D>
{
    fun configure() : D
}

inline fun <D, reified C: Configuration<D>> configure(noinline sideEffect:  C.()->Unit) : D {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configure()
}