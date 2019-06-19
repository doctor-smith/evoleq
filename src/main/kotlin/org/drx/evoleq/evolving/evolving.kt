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

import kotlinx.coroutines.*

/**
 * A cancellable scope enforcing structured concurrency
 * To be used as default by all implementations of the [Evolving] type
 */
val DefaultEvolvingScope: ()->CoroutineScope = { CoroutineScope(Job()) }

/* TODO make all evolvings cancellable in the sense that they have to implement the Cancellable interface */
interface Evolving<out D> {
    /**
     * The Job to be created when implementation is initialized
     */
    val job: Job

    /**
     * Get the evolved value
     */
    suspend fun get() : D
}

typealias LazyEvolving<D> = CoroutineScope.(D)->Evolving<D>

