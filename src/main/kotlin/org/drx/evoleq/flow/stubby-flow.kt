package org.drx.evoleq.flow

import org.drx.evoleq.evolving.Evolving

data class Stubby<D,C>(val data: D, val iota:(C)-> Evolving<D>, val stub:(C)-> Evolving<C>)

open class StubbyFlow<D,S,E,T>(

    val mainFlow: Flow<D, S>,
    val stubbyFlow: Flow<E, T>,
    val stubby: Stubby<D, E>
) : Flow<D, S>( mainFlow.conditions, mainFlow.flow) {

}