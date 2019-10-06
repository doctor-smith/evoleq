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
 * Explicitly unsuspended function
 * Usage: If you define a function as a lambda {s:S->t:T}, then the compiler dosn't know if the lambda is suspended or not
 */
fun <S, T> unSuspended(lambda :(S)->T): (S)->T = lambda
fun <S, T> standard(lambda:(S)->T): (S)->T = lambda

fun <S1, S2 ,T> standard(lambda: (S1,S2)->T): (S1,S2)->T = lambda
fun <S1, S2 ,S3, T> standard(lambda: (S1,S2,S3)->T): (S1,S2,S3)->T = lambda
fun <S1, S2 ,S3, S4, T> standard(lambda: (S1,S2,S3,S4)->T): (S1,S2,S3,S4)->T = lambda