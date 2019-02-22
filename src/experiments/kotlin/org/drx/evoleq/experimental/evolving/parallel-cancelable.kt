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
package org.drx.evoleq.experimental.evolving

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*
import org.drx.evoleq.evolving.Evolving
import kotlin.coroutines.CoroutineContext

/**
 * Implemented cancellation code in Parallel
 */
class ParallelCancelable<D>(private val delay: Long = 1,private val scope: CoroutineScope = GlobalScope,  private val block: suspend () -> D ): Evolving<D> {

    private val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    private var updated = false
    private  var job: Job
    init {
        val listener = ChangeListener<D>{_, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        property.addListener( listener )
        job = scope.launch {
            try {
                coroutineScope {
                    launch {
                        property.value = block()
                    }
                }
            } finally {
                property.removeListener(listener)
            }
        }

    }

    fun cancel(d: D) { if(!updated) {
        GlobalScope.launch {

            job.cancelAndJoin()
            //job.cancelChildren()
            property.value = d
            updated = true
        }}
    }

    override suspend fun get(): D {
        while(!updated){
            delay(delay)  // reason why get has to be suspended
        }
        return property.value
    }
}