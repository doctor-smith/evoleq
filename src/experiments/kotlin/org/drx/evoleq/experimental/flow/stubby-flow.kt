/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.experimental.flow

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Flow

data class Stubby<D,C>(val data: D, val iota:(C)-> Evolving<D>, val stub:(C)-> Evolving<C>)

open class StubbyFlow<D,S,E,T>(

    val mainFlow: Flow<D, S>,
    val stubbyFlow: Flow<E, T>,
    val stubby: Stubby<D, E>
) : Flow<D, S>( mainFlow.conditions, mainFlow.flow) {

}