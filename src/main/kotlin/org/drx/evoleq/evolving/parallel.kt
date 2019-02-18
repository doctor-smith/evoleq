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
import kotlinx.coroutines.NonCancellable.attachChild

/**
 * Evolution type parallel:
 * evolve async and parallel
 * - without blocking the current thread
 */
class Parallel<D>(
    private val delay: Long = 1,
    val scope: CoroutineScope = GlobalScope,
    private val block: suspend Parallel<D>.() -> D
) : Evolving<D> {

    private var deferred: Deferred<D>? = null

    init{
        scope.launch{ coroutineScope{
            deferred = async{ this@Parallel.block() }
        }}
    }

    override suspend fun get(): D {
        while(deferred == null) {
            delay(delay)
        }
        return deferred!!.await()
    }

    fun cancel(d: D): Evolving<D> = Immediate {
        while(deferred == null) {
            delay(delay)
        }
        deferred!!.cancel()
        d
    }

    fun job(): Job = deferred!!

/*
    private val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    private var updated = false
    private var job: Job? = null

    init {
        val listener = ChangeListener<D>{_, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        property.addListener( listener )

        job = scope.launch{ coroutineScope {
           launch {
                try {
                    property.value = block()
                } finally {
                    property.removeListener(listener)
                }
            }
        } }
    }

    override suspend fun get(): D {
        while(!updated){
            delay(delay)  // reason why get has to be suspended
        }
        return property.value
    }

    fun cancel(d: D) { if(!updated) {
        //scope.launch {

            job!!.cancel()//cancelAndJoin()
        job!!.cancelChildren()
            property.value = d
            updated = true
        //}
        }
    }

    fun job(): Job = job!!
*/
}
/**
 * Evolution type function
 */
/*
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
*/
