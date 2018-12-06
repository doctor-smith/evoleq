package org.drx.evoleq.old

import kotlinx.coroutines.Deferred
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.conditions.ok
import org.drx.evoleq.parallel
import org.drx.evoleq.conditions.update

/**
 * Old but nice
 * ====================================================================================================================
 */
/**
 * Old but nice - but not tailrec
 */
suspend fun <D, T> evolveOld1(
    data: D,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> Deferred<D>
): D = when ( conditions.ok() ) {
    false -> data
    true -> parallel {
        //  <- This makes it non-tail-recursive
        val newData: D = flow(data).await()
        evolveOld1(
            newData,
            conditions.update(newData)
        ) { data ->
            flow(data)
        }
    }
}

/**
 * Old but nice - and equivalent to the version old1
 */
suspend fun <D,T> evolveOld0(
    data: D,
    testObject: T,
    condition:(T)->Boolean,
    updateCondition: (D)-> T,
    flow: (D)-> Deferred<D>
): D = when ( condition( testObject ) ) {
    false -> data
    true -> parallel {
        val newData: D = flow(data).await()
        val newTestObject: T = updateCondition(newData)
        evolveOld0(
            newData,
            newTestObject,
            condition,
            updateCondition
        ) { data ->
            flow(data)
        }
    }
}

