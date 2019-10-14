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
package org.drx.evoleq.time

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel

interface WaitFor<D> {
    suspend fun toChange(): Evolving<D>
}

class WaitForProperty<D>(
    private val property: SimpleObjectProperty<D>,
    private val delay: Long = 1L,
    val scope: CoroutineScope = GlobalScope
) : WaitFor<D> {
    override suspend fun toChange(): Evolving<D> = scope.parallel {
        var changed = false
        val listener = ChangeListener<D> { _, _, _ -> changed = true }
        property.addListener(listener)
        while (!changed) {
            delay(delay)
        }
        property.removeListener(listener)
        property.value
    }
}

fun <D> CoroutineScope.waitForProperty(
    property: SimpleObjectProperty<D>,
    delay: Long = 1L
): WaitForProperty<D> =
    WaitForProperty(
        property,
        delay,
        this
    )

fun <K,V> Map<K,V>.waitForValueToBeSet(key: K): Evolving<V> =
    Parallel{
        GlobalScope.async {
            var v: V? = this@waitForValueToBeSet[key]
            while(v == null){
                delay(1)
            }
            v!!
        }.await()
    }

class Keeper<D>(var d: D?,val scope: CoroutineScope = GlobalScope)
fun<D> Keeper<D>.waitForValueToBeSet(): Evolving<D> =
scope.parallel{
    //GlobalScope.async {
        var v: D? = d
        while(v == null){
            delay(1)
        }
        v!!
    //}.await()
}

class Later<D>(val scope: CoroutineScope = GlobalScope){
    var value: D? = null
        set(value) {if(field == null) field = value}
}
fun <D> Later<D>.await(): Evolving<D> = scope.parallel{
    while(value == null) {
        delay(1)
    }
    value!!
}

fun <D> CoroutineScope.later(): Later<D> = Later(this)

class Change<D>(var value: D,val scope: CoroutineScope = GlobalScope)
fun <D> Change<D>.happen(): Evolving<D> = scope.parallel{
    val current = value
    while(value == current) {
        delay(1)
    }
    value
}

fun <D> CoroutineScope.change(value: D): Change<D> = Change(value, this)
