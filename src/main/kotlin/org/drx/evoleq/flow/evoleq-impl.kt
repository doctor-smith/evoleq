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
package org.drx.evoleq.flow

import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.conditions.Counter
import org.drx.evoleq.conditions.counting
import org.drx.evoleq.evolve

suspend fun<D> repeatParallel(times: Long,initialData: D, f:(D)->D):D =
    evolve<Pair<D, Long>, Long>(
        initialData = Pair(initialData, 0),
        conditions = Counter<D>(times).get()
    ) { pair ->
        Parallel { counting(f)(pair) }
    }.first
suspend fun<D> repeatImmediate(times: Long,initialData: D, f:(D)->D):D =
    evolve<Pair<D, Long>, Long>(
        initialData = Pair(initialData, 0),
        conditions = Counter<D>(times).get()
    ) { pair ->
        Immediate { counting(f)(pair) }
    }.first

