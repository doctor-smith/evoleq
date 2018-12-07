package org.drx.evoleq

import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.conditions.ok
import org.drx.evoleq.conditions.update
import org.drx.evoleq.evolving.Evolving

/**
 * Evolution equation
 */
tailrec suspend fun <D, T> evolve(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> Evolving<D>
) : D = when( conditions.ok() ) {
    false -> initialData
    true -> {
        val evolvedData: D = flow ( initialData ).get()
        evolve(
            evolvedData,
            conditions.update( evolvedData )
        ){
            data -> flow ( data )
        }
    }
}

/**
 * Evolution equation for sus
 */
tailrec suspend fun <D, T> evolveSuspended(
    initialData: D,
    conditions: EvolutionConditions<D, T>,
    flow: suspend (D) -> Evolving<D>
) : D = when( conditions.ok() ) {
    false -> initialData
    true -> {
        val evolvedData: D = flow ( initialData ).get()
        evolveSuspended(
            evolvedData,
            conditions.update( evolvedData )
        ){
            data  -> flow ( data )
        }
    }
}