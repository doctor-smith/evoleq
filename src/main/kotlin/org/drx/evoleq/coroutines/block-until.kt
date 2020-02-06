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
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.EvoleqDsl
import org.drx.evoleq.dsl.SmartArrayList

/**
 * Block coroutine execution until a property has the right value
 */
@EvoleqDsl
suspend fun <T : Any> blockUntil(property: Property<T>, predicate: (T)-> Boolean) =
    if(property.value == null || !predicate(property.value)) {
        try {
            val eternity: Deferred<Unit> = CoroutineScope(Job()).async {
                delay(Long.MAX_VALUE)
            }
            lateinit var listener: ChangeListener<T>
            fun removeListener() {
                property.removeListener(listener)
            }
            listener = ChangeListener { _, _, newValue ->
                if (predicate(newValue)) {
                    removeListener()
                    eternity.cancel()
                }
            }
            property.addListener(listener)
            if(property.value == null || !predicate(property.value)) {
                eternity.await()
            } else {
                removeListener()
                eternity.cancel()
            }
        } catch (exception: Exception) { }
    }  else { Unit }
/**
 * Block coroutine execution until a property has the right value
 */
@EvoleqDsl
suspend fun <T : Any> blockUntil(readOnlyProperty: ReadOnlyProperty<T>, predicate: (T)-> Boolean) =
    if(readOnlyProperty.value == null || !predicate(readOnlyProperty.value)) {
        try {
            val eternity: Deferred<Unit> = CoroutineScope(Job()).async {
                delay(Long.MAX_VALUE)
            }
            lateinit var listener: ChangeListener<T>
            fun removeListener() {
                readOnlyProperty.removeListener(listener)
            }
            listener = ChangeListener { _, _, newValue ->
                if (predicate(newValue)) {
                    removeListener()
                    eternity.cancel()
                }
            }
            readOnlyProperty.addListener(listener)
            if(readOnlyProperty.value == null || !predicate(readOnlyProperty.value)) {
                eternity.await()
            } else {
                removeListener()
                eternity.cancel()
            }
        } catch (exception: Exception) { }
    }  else { Unit }

/**
 * Block coroutine execution while a list is empty
 */
/*
@EvoleqDsl
suspend fun <T> ArrayList<T>.blockWhileEmpty(): Unit {
    if(isEmpty()) {
        val isNotEmpty = SimpleBooleanProperty(false)
        lateinit var listener: ListChangeListener<T>
        CoroutineScope(Job()).launch {coroutineScope{
            val observableList = FXCollections.observableList(this@blockWhileEmpty)
            listener = ListChangeListener<T> { change ->
                while (change.next()) {
                    if (change.wasAdded()) {
                        isNotEmpty.value = true
                        observableList.removeListener(listener)
                    }
                }
            }
            observableList.addListener(listener)
        } }
        blockUntil(isNotEmpty) { value -> value == true }
    }
}


 */
/**
 * Block coroutine execution while a [SmartArrayList] is empty
 */
@EvoleqDsl
suspend fun <T> SmartArrayList<T>.blockWhileEmpty() {
    blockUntil(isEmpty) {value -> !value}
}

/**
 * Block coroutine execution while a [SmartArrayList] is empty
 */
@EvoleqDsl
suspend fun <T> SmartArrayList<T>.blockWhileNonEmpty() {
    blockUntil(isEmpty) {value -> value}
}