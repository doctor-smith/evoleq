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
package org.drx.evoleq.dsl

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.evolving.Async
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel

fun <D> CoroutineScope.parallel(
    delay: Long = 1,
    block: suspend CoroutineScope.() -> D
): Parallel<D> = Parallel(delay,this){block()}

fun <D,E> Parallel<D>.parallel(
    delay: Long = 1,
    block: suspend CoroutineScope.() -> E
): Parallel<E> =Parallel(delay, this.scope) { block() }


fun <D> CoroutineScope.asynq(
    delay: Long = 1,
    block: suspend CoroutineScope.() -> D
): Evolving<D> = Async(delay,this){block()}