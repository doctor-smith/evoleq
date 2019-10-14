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
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Parallel

infix fun <S,T> Parallel<S>.mapParallel(f: (S)->T): Parallel<T> = scope.parallel { f(get()) }
suspend infix fun <S,T> Parallel<S>.mapParallel(f: suspend (S)->T): Parallel<T> = scope.parallel { f(get()) }
infix fun <S,T> Parallel<S>.mapParallel(f: suspend CoroutineScope.(S)->T): Parallel<T> = scope.parallel { f(get()) }

/**
 * TODO handle scopes / jobs in a convenient way
 */
suspend fun <D> muParallel(parallel: Parallel<Parallel<D>>) = parallel.scope.parallel {
    parallel/*.onScope(this)*/.get()
}.get()

/**
 * TODO handle scopes / jobs in a convenient way for all of the following 4 functions
 */
infix fun <R,S,T> ((R)->Parallel<S>).fishParallel(arrow:(S)->Parallel<T>):suspend (R)->Parallel<T> = {
        r -> muParallel(this@fishParallel(r) mapParallel suspended(arrow))
}
infix fun <R,S,T> (suspend (R)->Parallel<S>).fishParallel(arrow: suspend  (S)->Parallel<T>): suspend  (R)->Parallel<T> = {
        r -> muParallel(this(r) mapParallel arrow)
}
infix fun <R,S,T> ((R)->Parallel<S>).fishParallel(arrow: suspend  (S)->Parallel<T>): suspend  (R)->Parallel<T> = {
        r -> muParallel(this(r) mapParallel arrow)
}
infix fun <R,S,T> (suspend (R)->Parallel<S>).fishParallel(arrow: (S)->Parallel<T>): suspend  (R)->Parallel<T> = {
        r -> muParallel(this(r) mapParallel arrow)
}
