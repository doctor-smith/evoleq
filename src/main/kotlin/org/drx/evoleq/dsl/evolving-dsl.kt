package org.drx.evoleq.dsl

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.evolving.Async
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel

fun <D> CoroutineScope.parallel(
    delay: Long = 1,
    block: suspend Parallel<D>.() -> D
): Evolving<D> = Parallel(delay,this){block()}

fun <D> CoroutineScope.async(
    delay: Long = 1,
    block: suspend Async<D>.() -> D
): Evolving<D> = Async(delay,this){block()}