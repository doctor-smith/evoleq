package org.drx.evoleq

import kotlinx.coroutines.Deferred


/**
 * Flow of the evolution equation
 */
tailrec suspend fun <D,T,C > evolve(
    data: D,
    testObject: T,
    condition:(T)->Boolean,
    updateCondition: (D)-> T,
    flow: (D)->Deferred<D>
    ): D = when ( condition( testObject ) ) {
            false -> data
            true -> {
                val newData = flow( data ).await()
                val newTestObject = updateCondition( newData )
                evolve<D, T, C>(newData, newTestObject, condition, updateCondition, flow)
            }
    }

