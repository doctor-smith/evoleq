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
package org.drx.evoleq.util

import javafx.beans.binding.BooleanBinding
import javafx.beans.binding.DoubleBinding
import javafx.beans.property.Property
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


/**********************************************************************************************************************
 *
 * Boolean Property Bindings
 *
 **********************************************************************************************************************/

class NegationBinding(private val property: Property<Boolean>): BooleanBinding(){
    init{
        super.bind(property)
    }

    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Boolean = !property.value.fix()
}

class AndBinding(private val property1: Property<Boolean>,private val property2: Property<Boolean>): BooleanBinding(){
    init{
        super.bind(property1, property2)
    }

    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Boolean = property1.value.fix() && property2.value.fix()
}

class OrBinding(private val property1: Property<Boolean>,private val property2: Property<Boolean>): BooleanBinding(){
    init{
        super.bind(property1, property2)
    }

    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Boolean = property1.value.fix() || property2.value.fix()
}

class XorBinding(private val property1: Property<Boolean>, private val property2: Property<Boolean>): BooleanBinding() {
    init{
        super.bind(property1,property2)
    }

    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Boolean {
        val b1 = property1.value.fix()
        val b2 = property2.value.fix()
        return (b1 && !b2) || (!b1 && b2)
    }
}

fun Boolean?.fix(): Boolean = when(this){
    null -> false
    else -> this
}

/**********************************************************************************************************************
 *
 * Double Property Bindings
 *
 **********************************************************************************************************************/

fun Double?.fix(): Double = when(this) {
    null -> 0.0
    else -> this
}

class PlusDoubleBinding(private val property: Property<Double>, vararg properties: Property<Double>) : DoubleBinding() {
    private val props: Sequence<Property<Double>>
    init{
        super.bind(*properties)
        props = sequenceOf(property,*properties)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = props.map{p->p.value.fix()}.reduce{ v1, v2 -> v1 + v2 }
}

class MinusDoubleBinding(private val property1: Property<Double>, private val property2: Property<Double>): DoubleBinding() {
    init {
        super.bind(property1, property2)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = property1.value.fix() - property2.value.fix()
}

class TimesDoubleBinding(private val property: Property<Double>, vararg properties: Property<Double>) : DoubleBinding() {
    private val props: Sequence<Property<Double>>
    init{
        super.bind(*properties)
        props = sequenceOf(property,*properties)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = props.map{p->p.value.fix()}.reduce{ v1, v2 -> v1 * v2 }
}

class DivideDoubleBinding(private val numerator: Property<Double>, private val denominator: Property<Double>): DoubleBinding() {
    init {
        super.bind(numerator, denominator)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = numerator.value.fix() / denominator.value.fix()
}

class PowerDoubleBinding(private val base: Property<Double>, private val exponent: Property<Double>): DoubleBinding() {
    init {
        super.bind(base, exponent)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = base.value.fix().pow(exponent.value.fix())
}

class MaxDoubleBinding(private val property1: Property<Double>, private val property2: Property<Double>): DoubleBinding() {
    init {
        super.bind(property1, property2)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = max(property1.value.fix(),property2.value.fix())
}


class MinDoubleBinding(private val property1: Property<Double>, private val property2: Property<Double>): DoubleBinding() {
    init {
        super.bind(property1, property2)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = min(property1.value.fix(),property2.value.fix())
}

class InverseDoubleBinding(private val property: Property<Double>) : DoubleBinding() {
    init{
        super.bind(property)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = -property.value.fix()
}

class ReciprocalDoubleBinding(private val property: Property<Double>) : DoubleBinding() {
    init{
        super.bind(property)
    }
    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = 1/property.value.fix()
}

class FunctionDoubleBinding(private val property: Property<Double>,private val  f: (Double)->Double): DoubleBinding() {
    init{
        super.bind(property)
    }

    /**
     * Calculates the current value of this binding.
     * @return the current value
     */
    override fun computeValue(): Double = f(property.value.fix())
}

