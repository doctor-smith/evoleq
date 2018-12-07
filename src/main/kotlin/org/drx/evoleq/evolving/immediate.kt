package org.drx.evoleq.evolving

/**
 * Immediate: return immediately
 */
class Immediate<D>(private val block: suspend ()->D) : Evolving<D> {
    override suspend fun get(): D = block()
}