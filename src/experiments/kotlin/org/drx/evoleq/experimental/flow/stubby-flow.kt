package org.drx.evoleq.experimental.flow

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow

data class Stubby<D,C>(val data: D, val iota:(C)-> Evolving<D>, val stub:(C)-> Evolving<C>)

open class StubbyFlow<D,S,E,T>(

    val mainFlow: Flow<D, S>,
    val stubbyFlow: Flow<E, T>,
    val stubby: Stubby<D, E>
) : Flow<D, S>( mainFlow.conditions, mainFlow.flow) {

}