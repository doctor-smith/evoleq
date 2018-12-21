/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.examples.app_filesystem.fx

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.examples.application.fx.FxApp
import org.drx.evoleq.time.WaitForProperty

class ParallelFx<D>(private val delay: Long = 1, block:  ()-> D) : Evolving<D> {
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
                //val init = WaitForProperty(FxApp.TOOLKIT_INIT_PROPERTY).toChange().get()
                launch {
                    Platform.runLater {
                        property.value = block()
                        property.removeListener(listener)
                    }
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

fun <D> fx(block:()->D):D {

    var d:D? = null
    GlobalScope.launch {
        if(FxApp.TOOLKIT_INIT_PROPERTY.value == null) {
            val init = WaitForProperty(FxApp.TOOLKIT_INIT_PROPERTY).toChange()
        }
        coroutineScope {
            Platform.runLater {
                d = block()
            }
        }
    }
    while(d == null) {
        Thread.sleep(1)
    }
    return d!!
}
fun fxToolkit(): Boolean {
    var init = false
    GlobalScope.launch {
        init = WaitForProperty(FxApp.TOOLKIT_INIT_PROPERTY).toChange().get()
    }
    while(!init) {
        Thread.sleep(1)
    }
    return init
}