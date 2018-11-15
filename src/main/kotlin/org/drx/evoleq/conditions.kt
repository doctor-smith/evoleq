package org.drx.evoleq


/**
 * Evolution conditions for the evolution equation
 */
data class EvolutionConditions<in D, T>(
    val testObject: T,
    val check:(T)->Boolean,
    val updateCondition: (D) -> T
)

/**
 * Update the evolution contition
 */
fun<D,T> EvolutionConditions<D,T>.update(data: D): EvolutionConditions<D,T> =
    copy( testObject = updateCondition( data ) )

/**
 * Check test-object
 */
fun<D,T> EvolutionConditions<D,T>.ok(): Boolean = check( testObject )
