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
package org.drx.evoleq.coroutines

import javafx.beans.property.Property
import javafx.beans.property.ReadOnlyProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import org.drx.evoleq.dsl.EvoleqDsl

@EvoleqDsl
suspend fun <C: Any,T> runUntil(property: ReadOnlyProperty<C>, predicate: (C)->Boolean, block: suspend CoroutineScope.()-> T): T? = try{
    if(property.value == null || !predicate(property.value)) {
        val scope = CoroutineScope(Job())
        var result: T? = null
        val cancelledOrCompleted = SimpleBooleanProperty(false)
        val listener = ChangeListener<C> { _, _, newValue ->
            if (predicate(newValue)) {
                cancelledOrCompleted.value = true
                scope.cancel()
            }
        }
        property.addListener (listener)
        scope.async{
            result = block()
            cancelledOrCompleted.value = true
        }
        blockUntil(cancelledOrCompleted){value -> value == true}
        property.removeListener(listener)
        result
    } else {
        null
    }
} catch (exception: Exception){ null }

/**********************************************************************************************************************
 *
 * Auxiliary function
 *
 **********************************************************************************************************************/

/**
 * Auxiliary function
 */
@EvoleqDsl
fun <T> CoroutineScope.run(block: suspend CoroutineScope.()->T): suspend CoroutineScope.()->T = block

/**
 * Auxiliary function
 */
@EvoleqDsl
suspend fun <C: Any, T> (suspend CoroutineScope.()->T).until(property: Property<C>, predicate: (C)->Boolean): T?
        = runUntil(property,predicate,this)

/**
 * Auxiliary function
 */
@EvoleqDsl
suspend infix fun <C: Any, T> (suspend CoroutineScope.()->T).until(condition: Pair<ReadOnlyProperty<C>, (C)->Boolean>): T?
        = runUntil(condition.first,condition.second,this)

/**
 * Auxiliary function
 */
@EvoleqDsl
suspend infix fun <C: Any, T> (suspend CoroutineScope.()->T).asLongAs(condition: Pair<ReadOnlyProperty<C>, (C)->Boolean>): T?
        = runUntil(condition.first,{c -> !condition.second(c)},this)

/**
 * Auxiliary function
 */
@EvoleqDsl
infix fun <C:Any> Property<C>.fulfills(predicate: (C) -> Boolean): Pair<Property<C>, (C)->Boolean> = Pair(this,predicate)

/**
 * Auxiliary function
 */
@EvoleqDsl
infix fun ReadOnlyProperty<Boolean>.fulfills(predicate: (Boolean) -> Boolean): Pair<ReadOnlyProperty<Boolean>, (Boolean)->Boolean> = Pair(this,predicate)

/**
 * Auxiliary function
 */
@EvoleqDsl
fun Property<Boolean>.isTrue(): Pair<ReadOnlyProperty<Boolean>, (Boolean) -> Boolean> = this fulfills { x -> x }

/**
 * Auxiliary function
 */
@EvoleqDsl
fun Property<Boolean>.isFalse(): Pair<ReadOnlyProperty<Boolean>, (Boolean) -> Boolean> = this fulfills { x -> !x }


/**
 * Auxiliary function
 */
@EvoleqDsl
fun ReadOnlyProperty<Boolean>.isTrue(): Pair<ReadOnlyProperty<Boolean>, (Boolean)->Boolean> = this fulfills { x -> x }

/**
 * Auxiliary function
 */
@EvoleqDsl
fun ReadOnlyProperty<Boolean>.isFalse(): Pair<ReadOnlyProperty<Boolean>, (Boolean)->Boolean> = this fulfills { x -> !x }

