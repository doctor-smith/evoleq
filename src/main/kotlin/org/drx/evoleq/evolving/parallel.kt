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
package org.drx.evoleq.evolving

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*

/**
 * Evolution type parallel:
 * evolve async and parallel
 * - without blocking the current thread
 */
class Parallel<D>(private val delay: Long = 1, private val block: suspend () -> D ) :
    Evolving<D> {

    private val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    private var updated = false
    init {
        val listener = ChangeListener<D>{_, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        property.addListener( listener )
        GlobalScope.launch {
            coroutineScope {
                launch {
                    property.value = block()
                    property.removeListener( listener )
                }
            }
        }
    }
    override suspend fun get(): D {
        while(!updated){
            delay(delay)  // reason why get has to be suspended
        }
        return property.value
    }
}
/**
 * Evolution type function
 */
suspend fun <D> parallel( block: suspend () -> D ): D {
    val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    var updated = false
    property.addListener{ _, oV, nV ->
        if(nV != oV) {
            updated = true
        }
    }
    coroutineScope{ launch {
        property.value = block()
    }}
    return GlobalScope.async {
        while(!updated){
            delay(1)
        }
        val result = property.value
        result
    }.await()
}
