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

import kotlinx.coroutines.CoroutineScope

fun <S> onScope(f:()->S): CoroutineScope.()->S = { f() }
fun <S,T> onScope(f:(S)->T): CoroutineScope.(S)->T = { d -> f(d) }
fun <S1,S2,T> onScope(f:(S1,S2)->T): CoroutineScope.(S1,S2)->T = { s1, s2 -> f(s1,s2) }
fun <S1,S2,S3,T> onScope(f:(S1,S2,S3)->T): CoroutineScope.(S1,S2,S3)->T = { s1, s2, s3 -> f(s1,s2,s3) }
fun <S1,S2,S3,S4,T> onScope(f:(S1,S2,S3,S4)->T): CoroutineScope.(S1,S2,S3,S4)->T = { s1, s2, s3, s4 -> f(s1,s2,s3,s4) }
fun <S1,S2,S3,S4,S5,T> onScope(f:(S1,S2,S3,S4,S5)->T): CoroutineScope.(S1,S2,S3,S4,S5)->T = { s1, s2, s3, s4, s5 -> f(s1,s2,s3,s4, s5) }
fun <S1,S2,S3,S4,S5,S6,T> onScope(f:(S1,S2,S3,S4,S5,S6)->T): CoroutineScope.(S1,S2,S3,S4,S5,S6)->T = { s1, s2, s3, s4, s5, s6 -> f(s1,s2,s3,s4,s5,s6) }
fun <S1,S2,S3,S4,S5,S6,S7,T> onScope(f:(S1,S2,S3,S4,S5,S6,S7)->T): CoroutineScope.(S1,S2,S3,S4,S5,S6,S7)->T = { s1, s2, s3, s4, s5, s6, s7 -> f(s1,s2,s3,s4,s5,s6,s7) }
fun <S1,S2,S3,S4,S5,S6,S7,S8,T> onScope(f:(S1,S2,S3,S4,S5,S6,S7,S8)->T): CoroutineScope.(S1,S2,S3,S4,S5,S6,S7,S8)->T = { s1, s2, s3, s4, s5, s6, s7, s8 -> f(s1,s2,s3,s4,s5,s6,s7,s8) }
fun <S1,S2,S3,S4,S5,S6,S7,S8,S9,T> onScope(f:(S1,S2,S3,S4,S5,S6,S7,S8,S9)->T): CoroutineScope.(S1,S2,S3,S4,S5,S6,S7,S8,S9)->T = { s1, s2, s3, s4, s5, s6, s7, s8, s9 -> f(s1,s2,s3,s4,s5,s6,s7,s8,s9) }
fun <S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,T> onScope(f:(S1,S2,S3,S4,S5,S6,S7,S8,S9,S10)->T): CoroutineScope.(S1,S2,S3,S4,S5,S6,S7,S8,S9,S10)->T = { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10 -> f(s1,s2,s3,s4,s5,s6,s7,s8,s9,s10) }



fun <S> onScopeSuspended(f: suspend ()->S): suspend CoroutineScope.()->S = { f() }
fun <S,T> onScopeSuspended(f: suspend (S)->T): suspend CoroutineScope.(S)->T = { d -> f(d) }
fun <S1,S2,T> onScopeSuspended(f: suspend (S1,S2)->T): suspend CoroutineScope.(S1,S2)->T = { s1, s2 -> f(s1,s2) }
fun <S1,S2,S3,T> onScopeSuspended(f: suspend (S1,S2,S3)->T): suspend CoroutineScope.(S1,S2,S3)->T = { s1, s2, s3 -> f(s1,s2,s3) }
fun <S1,S2,S3,S4,T> onScopeSuspended(f: suspend (S1,S2,S3,S4)->T): suspend CoroutineScope.(S1,S2,S3,S4)->T = { s1, s2, s3, s4 -> f(s1,s2,s3,s4) }
fun <S1,S2,S3,S4,S5,T> onScopeSuspended(f: suspend (S1,S2,S3,S4,S5)->T): suspend CoroutineScope.(S1,S2,S3,S4,S5)->T = { s1, s2, s3, s4, s5 -> f(s1,s2,s3,s4, s5) }
fun <S1,S2,S3,S4,S5,S6,T> onScopeSuspended(f: suspend (S1,S2,S3,S4,S5,S6)->T): suspend CoroutineScope.(S1,S2,S3,S4,S5,S6)->T = { s1, s2, s3, s4, s5, s6 -> f(s1,s2,s3,s4,s5,s6) }
fun <S1,S2,S3,S4,S5,S6,S7,T> onScopeSuspended(f: suspend (S1,S2,S3,S4,S5,S6,S7)->T): suspend CoroutineScope.(S1,S2,S3,S4,S5,S6,S7)->T = { s1, s2, s3, s4, s5, s6, s7 -> f(s1,s2,s3,s4,s5,s6,s7) }
fun <S1,S2,S3,S4,S5,S6,S7,S8,T> onScopeSuspended(f: suspend (S1,S2,S3,S4,S5,S6,S7,S8)->T): suspend CoroutineScope.(S1,S2,S3,S4,S5,S6,S7,S8)->T = { s1, s2, s3, s4, s5, s6, s7, s8 -> f(s1,s2,s3,s4,s5,s6,s7,s8) }
fun <S1,S2,S3,S4,S5,S6,S7,S8,S9,T> onScopeSuspended(f: suspend (S1,S2,S3,S4,S5,S6,S7,S8,S9)->T): suspend CoroutineScope.(S1,S2,S3,S4,S5,S6,S7,S8,S9)->T = { s1, s2, s3, s4, s5, s6, s7, s8, s9 -> f(s1,s2,s3,s4,s5,s6,s7,s8,s9) }
fun <S1,S2,S3,S4,S5,S6,S7,S8,S9,S10,T> onScopeSuspended(f: suspend (S1,S2,S3,S4,S5,S6,S7,S8,S9,S10)->T): suspend CoroutineScope.(S1,S2,S3,S4,S5,S6,S7,S8,S9,S10)->T = { s1, s2, s3, s4, s5, s6, s7, s8, s9, s10 -> f(s1,s2,s3,s4,s5,s6,s7,s8,s9,s10) }

fun <S, T> on(scope: CoroutineScope, block: suspend CoroutineScope.(S)->T): suspend (S)->T = {s -> scope.block(s)}

/*

fun <S,T> CoroutineScope.onScope(f: (CoroutineContext)-> (S)->T): CoroutineScope.(CoroutineContext, S)->T =
    { context, s -> this@onScope + context; f(context)( s ) }

fun <S1,S2,T> CoroutineScope.onScope(f: (CoroutineContext)-> (S1,S2)->T): CoroutineScope.(CoroutineContext, S1,S2)->T =
    { context, s1,s2 -> this@onScope + context; f(context)( s1, s2 ) }
*/

//operator fun CoroutineScope.plus(scope: CoroutineScope) = this + scope.coroutineContext
