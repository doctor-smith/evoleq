package org.drx.evoleq

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*


/**
 * Flow of the evolution equation
 */
suspend fun <D, T> evolve(
    data: D,
    conditions: EvolutionConditions<D, T>,
    flow: (D) -> Deferred<D>
): D = when ( conditions.ok() ) {
    false -> data
    true ->  parallel {   //  <- This makes it non-tail-recursive
        val newData: D = flow ( data ).await()
        evolve(
            newData,
            conditions.update( newData )
        ){
            data -> flow ( data )
        }
    }
}

/**
 * Evolution type
 */
suspend fun <D> parallel( block: suspend () -> D ): D {
    val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    var updated = false
    property.addListener{ _, oV, nV ->
        if(nV != oV) {
            updated = true
        }
    }
    coroutineScope{ launch {
        property.value = block()
    }}
    return GlobalScope.async {
        while(!updated){
            delay(1)
        }
        val result = property.value
        result
    }.await()
}

/**
 * Evolution conditions for the evolution equation
 */
data class EvolutionConditions<D,T>(
    val testObject: T,
    val check:(T)->Boolean,
    val updateCondition: (D) -> T
)

/**
 * Update the evolution contition
 */
fun<D,T> EvolutionConditions<D,T>.update(d: D): EvolutionConditions<D,T> =
    copy( testObject = updateCondition( d ) )

/**
 * Check test-object
 */
fun<D,T> EvolutionConditions<D,T>.ok(): Boolean = check( testObject )


/**
 * Old but nice - and equivalent to the new version
 */
suspend fun <D,T> evolveOld(
    data: D,
    testObject: T,
    condition:(T)->Boolean,
    updateCondition: (D)-> T,
    flow: (D)->Deferred<D>
): D = when ( condition( testObject ) ) {
    false -> data
    true ->  parallel {
        val newData: D = flow(data).await()
        val newTestObject: T = updateCondition(newData)
        evolveOld(
            newData,
            newTestObject,
            condition,
            updateCondition
        ){
                data -> flow ( data )
        }
    }
}

