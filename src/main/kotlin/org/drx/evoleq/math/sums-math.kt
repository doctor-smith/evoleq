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

import org.drx.evoleq.data.Sum

fun <S2, S1, T> sum(f2: (S2) -> T, f1: (S1) -> T): (Sum<S2, S1>) -> T = { sum ->
    when (sum) {
        is Sum.First -> f2(sum.value)
        is Sum.Second -> f1(sum.value)
    }
}

fun <S3, S2, S1, T> sum(f3: (S3) -> T, f2: (S2) -> T, f1: (S1) -> T): (Sum<S3, Sum<S2, S1>>) -> T = { sum ->
    when (sum) {
        is Sum.First -> f3(sum.value)
        is Sum.Second -> sum(f2, f1)(sum.value)
    }
}

fun <S4, S3, S2, S1, T> sum(
    f4: (S4) -> T,
    f3: (S3) -> T,
    f2: (S2) -> T,
    f1: (S1) -> T
): (Sum<S4, Sum<S3, Sum<S2, S1>>>) -> T = { sum ->
    when (sum) {
        is Sum.First -> f4(sum.value)
        is Sum.Second -> sum(f3, f2, f1)(sum.value)
    }
}

fun <S5, S4, S3, S2, S1, T> sum(
    f5: (S5) -> T,
    f4: (S4) -> T,
    f3: (S3) -> T,
    f2: (S2) -> T,
    f1: (S1) -> T
): (Sum<S5, Sum<S4, Sum<S3, Sum<S2, S1>>>>) -> T = { sum ->
    when (sum) {
        is Sum.First -> f5(sum.value)
        is Sum.Second -> sum(f4, f3, f2, f1)(sum.value)
    }
}

fun <S6, S5, S4, S3, S2, S1, T> sum(
    f6: (S6) -> T,
    f5: (S5) -> T,
    f4: (S4) -> T,
    f3: (S3) -> T,
    f2: (S2) -> T,
    f1: (S1) -> T
): (Sum<S6, Sum<S5, Sum<S4, Sum<S3, Sum<S2, S1>>>>>) -> T = { sum ->
    when (sum) {
        is Sum.First -> f6(sum.value)
        is Sum.Second -> sum(f5, f4, f3, f2, f1)(sum.value)
    }
}


