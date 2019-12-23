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

import javafx.beans.property.*

typealias IntProperty = Property<Int>

@Suppress("unchecked_cast")
fun intProperty(initialValue: Int? = null): IntProperty = when(initialValue){
    null -> SimpleIntegerProperty()
    else -> SimpleIntegerProperty(initialValue)
} as IntProperty

fun doubleProperty(initialValue: Double? = null): SimpleDoubleProperty = when(initialValue){
    null -> SimpleDoubleProperty()
    else -> SimpleDoubleProperty(initialValue)
}

fun stringProperty(initialValue: String? = null): SimpleStringProperty = when(initialValue) {
    null -> SimpleStringProperty()
    else -> SimpleStringProperty(initialValue)
}

fun booleanProperty(initialValue: Boolean? = null): SimpleBooleanProperty = when(initialValue) {
    null -> SimpleBooleanProperty()
    else -> SimpleBooleanProperty(initialValue)
}

/**********************************************************************************************************************
 *
 * Boolean Property Operations
 *
 **********************************************************************************************************************/

infix fun BooleanProperty.and(other: BooleanProperty): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(AndBinding(this@and,other))
    this
}

infix fun BooleanProperty.or(other: BooleanProperty): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(OrBinding(this@or,other))
    this
}

operator fun BooleanProperty.not(): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(NegationBinding(this@not))
    this
}

infix fun BooleanProperty.xor(other: BooleanProperty): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(XorBinding(this@xor,other))
    this
}

/**********************************************************************************************************************
 *
 * Double Property Arithmetic
 *
 **********************************************************************************************************************/

operator fun Property<Double>.plus(other: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(PlusDoubleBinding(this@plus, other))
    this
}

operator fun Property<Double>.minus(other: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(MinusDoubleBinding(this@minus, other))
    this
}

operator fun Property<Double>.times(other: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(TimesDoubleBinding(this@times, other))
    this
}

operator fun Property<Double>.div(denominator: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(DivideDoubleBinding(this@div, denominator))
    this
}

infix fun Property<Double>.toThe(exponent: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(PowerDoubleBinding(this@toThe, exponent))
    this
}

fun max(property1: Property<Double>, property2: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(MaxDoubleBinding(property1, property2))
    this
}

fun min(property1: Property<Double>, property2: Property<Double>): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(MinDoubleBinding(property1, property2))
    this
}

operator fun Property<Double>.unaryMinus(): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(InverseDoubleBinding(this@unaryMinus))
    this
}

fun Property<Double>.reciprocal(): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(ReciprocalDoubleBinding(this@reciprocal))
    this
}

operator fun Double.div(property: Property<Double>) : ReadOnlyDoubleProperty = property.reciprocal()

infix fun Property<Double>.apply(f:(Double)->Double): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(FunctionDoubleBinding(this@apply, f))
    this
}