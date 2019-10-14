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
package org.drx.evoleq.gap

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.LazyEvolving

data class LazyGap<W,P>(
    //val scope: CoroutineScope,
    val from: suspend CoroutineScope.(W)-> Evolving<P>,
    val to: suspend CoroutineScope.(W)->CoroutineScope.(P)->Evolving<W>
)

suspend fun <W,P> LazyGap<W, P>.fill(filler: LazyEvolving<P>): LazyEvolving<W> = {
    w: W -> parallel{
        this.to(w)(filler(from(w).get()).get()).get()
    }
}