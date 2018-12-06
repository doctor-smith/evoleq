package org.drx.evoleq.conditions


/**
 * Evolution conditions for the evolution equation
 */
data class EvolutionConditions<in D, T>(
    val testObject: T,
    val check:(T)->Boolean,
    val updateCondition: (D) -> T
)

/**
 * Update the evolution condition
 */
fun<D,T> EvolutionConditions<D, T>.update(data: D): EvolutionConditions<D, T> =
    copy( testObject = updateCondition( data ) )

/**
 * Check test-object
 */
fun<D,T> EvolutionConditions<D, T>.ok(): Boolean = check( testObject )

/**
 * Run flow only once
 */
fun <D> once(): EvolutionConditions<D, Boolean> =
    EvolutionConditions(
        testObject = true,
        check = { b -> b },
        updateCondition = { d: D -> false }
    )

/**
 * Run flow for a given amount of times
 */
class Counter<D>(private val to: Long) {
    fun get(): EvolutionConditions<Pair<D, Long>, Long> =
        EvolutionConditions(
            testObject = 0L,
            check = { l -> l < to }
        ) {
            pair -> pair.second
        }
}

fun <D> counting(f: (D)->D): (Pair<D,Long>)->Pair<D,Long> = {
    pair -> Pair(f(pair.first), pair.second +1)
}


