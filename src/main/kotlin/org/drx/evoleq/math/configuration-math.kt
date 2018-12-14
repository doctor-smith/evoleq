package org.drx.evoleq.math


inline fun <reified C, reified D: C>
consumeConfig(
    consumer: D,
    noinline consume: C.()->Unit
): D {
    consumer.consume()
    return consumer
}
