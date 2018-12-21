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
package org.drx.evoleq.coroutines

/**
 * Suspend an ordinary function
 */
fun <S,T> suspended(f:(S)->T): suspend (S)->T  {
    val suspended: suspend (S)->T
    suspended = { s -> f(s)}
    return suspended
}
fun<T> T.suspended(): suspend ()->T  {
    return {this}
}

fun <S, T> suspended(vararg functions: (S)->T): SuspendedFunctions<S,T> = SuspendedFunctions(*functions.map{f -> suspended(f)}.toTypedArray())


class SuspendedFunctions<S,T>(vararg functions: suspend (S)->T) {
    val functions = functions
    fun isEmpty(): Boolean = functions.isEmpty()
    fun first(): suspend (S)->T = functions[0]

    fun tail(): SuspendedFunctions<S,T> {
        val list = arrayListOf<suspend (S)->T>()
        IntRange(1,functions.size-1).forEach { list.add(functions[it]) }
        return SuspendedFunctions(*list.toTypedArray())
    }
}