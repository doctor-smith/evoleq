package org.drx.evoleq.evolving


interface Evolving<out D> {
    suspend fun get() : D
}


