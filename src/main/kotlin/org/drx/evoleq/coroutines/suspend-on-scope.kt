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


fun <S, T> suspendOnScope(f:(S)->T): suspend CoroutineScope.(S)->T = {
        s -> f(s)
}

fun <S1,S2, T> suspendOnScope(f:(S1,S2)->T): suspend CoroutineScope.(S1, S2)->T = {
        s1,s2 -> f(s1,s2)
}

fun <S1,S2,S3, T> suspendOnScope(f:(S1,S2,S3)->T): suspend CoroutineScope.(S1, S2, S3)->T = {
        s1,s2,s3 -> f(s1,s2,s3)
}

fun <S1,S2,S3,S4, T> suspendOnScope(f:(S1,S2,S3,S4)->T): suspend CoroutineScope.(S1, S2, S3, S4)->T = {
        s1,s2,s3, s4 -> f(s1,s2,s3,s4)
}

fun <S1,S2,S3,S4,S5, T> suspendOnScope(f:(S1,S2,S3,S4,S5)->T): suspend CoroutineScope.(S1, S2, S3, S4, S5)->T = {
        s1,s2,s3,s4,s5 -> f(s1,s2,s3,s4,s5)
}