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
package org.drx.evoleq.experimental

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

abstract class Context<S,T>() {

    //abstract fun capture(f: (S)->T)
    abstract fun run(): T
}

class ParallelContext<S,T>(val s:S, val f:(S)->T) : Context<S,T>() {
    private val property: SimpleObjectProperty<T> = SimpleObjectProperty()
    private var updated = false

    init {
        val listener = ChangeListener<T>{_, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        property.addListener( listener )
        GlobalScope.launch {
            coroutineScope {
                launch {
                    property.value = f(s)
                    property.removeListener( listener )
                }
            }
        }

    }

    override fun run(): T {
        while(!updated){
            sleep(1)
        }
        return property.value
    }

}