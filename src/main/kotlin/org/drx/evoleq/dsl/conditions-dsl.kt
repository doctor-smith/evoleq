package org.drx.evoleq.dsl

import org.drx.evoleq.conditions.EvolutionConditions

class EvolutionConditionsConfiguration<D, T> : Configuration<EvolutionConditions<D, T>> {

    var testObject: T? = null
    var check:((T)->Boolean)? = null
    var updateCondition: ((D) -> T)? = null

    override fun configure(): EvolutionConditions<D, T> =
        EvolutionConditions(
            testObject = testObject!!,
            check = check!!,
            updateCondition = updateCondition!!
        )
}
fun <D,T> conditions(configure: EvolutionConditionsConfiguration<D,T>.()->Unit) : EvolutionConditions<D, T> = configure(configure)