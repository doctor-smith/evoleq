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
package org.drx.evoleq.coroutines


/**
 * Suspend an ordinary function
 */
fun <S,T> suspended(f:(S)->T): suspend (S)->T  {
    val suspended: suspend (S)->T
    suspended = { s -> f(s) }
    return suspended
}
fun <S1, S2 ,T> suspended(lambda: (S1,S2)->T): suspend (S1,S2)->T = { s1, s2-> lambda(s1,s2) }
fun <S1, S2 ,S3, T> suspended(lambda: (S1,S2,S3)->T): suspend (S1,S2,S3)->T = { s1, s2, s3 -> lambda(s1,s2,s3) }
fun <S1, S2 ,S3, S4, T> suspended(lambda: (S1,S2,S3,S4)->T): suspend (S1,S2,S3,S4)->T = { s1, s2, s3,s4 -> lambda(s1,s2,s3,s4) }

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

