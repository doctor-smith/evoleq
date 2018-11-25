package org.drx.evoleq

interface Evolver<D> {
    suspend fun evolve(d: D): Evolving<D>
}
data class Stubby<D,C>(val data: D,val iota:(C)->Evolving<D>,val stub:(C)->Evolving<C>)



data class Flow<D, T>(
    val conditions: EvolutionConditions<D, T>,
    val flow: (D)->Evolving<D>
) : Evolver<D> {
    override suspend fun evolve(data: D): Evolving<D> =
        Immediate {
            evolve(
                initialData = data,
                conditions = conditions
            ){
                data -> flow ( data )
            }
        }
}


