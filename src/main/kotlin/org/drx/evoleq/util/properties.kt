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
import javafx.beans.value.WritableValue
import org.drx.evoleq.dsl.EvoleqDsl

typealias IntProperty = Property<Int>

@Suppress("unchecked_cast")
@EvoleqDsl
fun intProperty(initialValue: Int? = null): IntProperty = when(initialValue){
    null -> SimpleIntegerProperty()
    else -> SimpleIntegerProperty(initialValue)
} as IntProperty

@EvoleqDsl
fun doubleProperty(initialValue: Double? = null): SimpleDoubleProperty = when(initialValue){
    null -> SimpleDoubleProperty()
    else -> SimpleDoubleProperty(initialValue)
}
@EvoleqDsl

fun stringProperty(initialValue: String? = null): SimpleStringProperty = when(initialValue) {
    null -> SimpleStringProperty()
    else -> SimpleStringProperty(initialValue)
}

@EvoleqDsl
fun booleanProperty(initialValue: Boolean? = null): SimpleBooleanProperty = when(initialValue) {
    null -> SimpleBooleanProperty()
    else -> SimpleBooleanProperty(initialValue)
}

/**********************************************************************************************************************
 *
 * Boolean Property Operations
 *
 **********************************************************************************************************************/

@EvoleqDsl
infix fun ReadOnlyBooleanProperty.and(other: ReadOnlyBooleanProperty): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(AndBinding(this@and,other))
    this
}

@EvoleqDsl
infix fun ReadOnlyBooleanProperty.or(other: ReadOnlyBooleanProperty): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(OrBinding(this@or,other))
    this
}

@EvoleqDsl
operator fun ReadOnlyBooleanProperty.not(): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(NegationBinding(this@not))
    this
}

@EvoleqDsl
infix fun ReadOnlyBooleanProperty.xor(other: ReadOnlyBooleanProperty): ReadOnlyBooleanProperty = with(booleanProperty()) {
    bind(XorBinding(this@xor,other))
    this
}

/**********************************************************************************************************************
 *
 * Double Property Arithmetic
 *
 **********************************************************************************************************************/

@EvoleqDsl
operator fun ReadOnlyDoubleProperty.plus(other: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(PlusDoubleBinding(this@plus, other))
    this
}

@EvoleqDsl
operator fun ReadOnlyDoubleProperty.minus(other: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(MinusDoubleBinding(this@minus, other))
    this
}

@EvoleqDsl
operator fun ReadOnlyDoubleProperty.times(other: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(TimesDoubleBinding(this@times, other))
    this
}

@EvoleqDsl
operator fun ReadOnlyDoubleProperty.div(denominator: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(DivideDoubleBinding(this@div, denominator))
    this
}

@EvoleqDsl
infix fun ReadOnlyDoubleProperty.toThe(exponent: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()){
    bind(PowerDoubleBinding(this@toThe, exponent))
    this
}

@EvoleqDsl
fun max(property1: ReadOnlyDoubleProperty, property2: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(MaxDoubleBinding(property1, property2))
    this
}

@EvoleqDsl
fun min(property1: ReadOnlyDoubleProperty, property2: ReadOnlyDoubleProperty): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(MinDoubleBinding(property1, property2))
    this
}

@EvoleqDsl
operator fun ReadOnlyDoubleProperty.unaryMinus(): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(InverseDoubleBinding(this@unaryMinus))
    this
}

@EvoleqDsl
fun ReadOnlyDoubleProperty.reciprocal(): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(ReciprocalDoubleBinding(this@reciprocal))
    this
}

@EvoleqDsl
operator fun Double.div(property: ReadOnlyDoubleProperty) : ReadOnlyDoubleProperty = property.reciprocal()

@EvoleqDsl
infix fun ReadOnlyDoubleProperty.apply(f:(Double)->Double): ReadOnlyDoubleProperty = with(doubleProperty()) {
    bind(FunctionDoubleBinding(this@apply, f))
    this
}


/**********************************************************************************************************************
 *
 * Functional JavaFX Properties
 *
 **********************************************************************************************************************/

/**
 * Functional FxProperty setter
 */
@EvoleqDsl
fun <T> WritableValue<T>.setNullSave(update: T?.()->T): WritableValue<T> = with(this) {
    value = value.update()
    this
}

/**
 * Functional FxProperty setter
 */
@EvoleqDsl
suspend fun <T> WritableValue<T>.setNullSaveSuspended(update: suspend T?.()->T): WritableValue<T> = with(this) {
    value = value.update()
    this
}

@EvoleqDsl
fun <T> WritableValue<T>.set(update: T.()->T): WritableValue<T> = with(this) {
    value = value.update()
    this
}

@EvoleqDsl
suspend fun <T> WritableValue<T>.setSuspended(update: suspend T.()->T): WritableValue<T> = with(this) {
    value = value.update()
    this
}

@EvoleqDsl
fun <S,T> Property<S>.bind(f: S.()->T): Property<T> {

    val property = SimpleObjectProperty<T>()

    addListener { _, _, newValue ->
        property.value = value.f()
    }
    return property
}

@EvoleqDsl
fun <T> Property<T>.valueIsNull(): BooleanProperty {
    val property = booleanProperty()
    property.value = value == null
    addListener { _,_,newValue ->
        property.set{ newValue == null }
    }
    return property
}

@EvoleqDsl
fun <T> Property<T>.valueIsNotNull(): BooleanProperty
{
    val property = booleanProperty()
    property.value = value != null
    addListener { _,_,newValue ->
        property.set{ newValue != null }
    }
    return property
}

