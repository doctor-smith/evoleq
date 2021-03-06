/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    fun testObject(testObject: T) {
        this.testObject = testObject
    }

    fun check(check: (T)->Boolean) {
        this.check = check
    }

    fun updateCondition(update: (D)->T) {
        updateCondition = update
    }

}
fun <D,T> conditions(configure: EvolutionConditionsConfiguration<D,T>.()->Unit) : EvolutionConditions<D, T> = configure(configure)