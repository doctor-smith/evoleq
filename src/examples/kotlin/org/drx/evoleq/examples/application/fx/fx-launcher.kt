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
package org.drx.evoleq.examples.application.fx

import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.time.WaitForProperty


class FxApplicationLauncher<Stub>(val stubProperty: SimpleObjectProperty<Stub> ) {
    suspend inline fun <reified C: Application> launch(): Evolving<Stub> {

        GlobalScope.launch {
            coroutineScope{
                Application.launch(C::class.java)
            }
        }
        return WaitForProperty(stubProperty).toChange()
    }
}
