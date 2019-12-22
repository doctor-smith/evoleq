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
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.SmartArrayList

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
            //eternity.await()

            if(property.value == null || !predicate(property.value)) {
                eternity.await()
            } else {
                removeListener()
                eternity.cancel()
            }
        } catch (exception: Exception) { }
    }  else { Unit }

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
            //println("change listener added")
        } }
        blockUntil(isNotEmpty) { value -> value == true }

    }
}

suspend fun <T> SmartArrayList<T>.blockWhileEmpty() {
    blockUntil(isEmpty) {value -> value == false}
}

fun <T> ArrayList<T>.onAdd(f: ()->Unit): ArrayList<T> = object: ArrayList<T>() {
    override fun add(element: T): Boolean {
        val ret = super.add(element)
        f()
        return ret
    }
}