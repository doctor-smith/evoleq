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
package org.drx.evoleq.math

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Evolver

class Conjugation<S,T>(val invert:(T)->S, val forward:(S)->T)

fun <D,E> Evolver<D>.conjugate(f: Conjugation<D,E>): Evolver<E> {
    return object: Evolver<E> {

        override val scope: CoroutineScope
            get() = this@conjugate.scope

        override suspend fun evolve(e: E): Evolving<E> {
            return this@conjugate.evolve(f.invert(e)).map (f.forward)
        }
    }
}