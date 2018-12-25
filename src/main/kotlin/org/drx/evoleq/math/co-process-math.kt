/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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

import org.drx.evoleq.coroutines.SuspendedFunctions
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.util.tail

suspend fun <S,T> coProcess(first: (Evolving<S>)-> T, vararg steps: (Evolving<T>)-> T):  (Evolving<S>)-> T =
    when(steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = arrayListOf(*steps).tail()
            coProcess(first / next, tail)
        }
    }
tailrec suspend fun <S,T> coProcess(first:  (Evolving<S>)-> T, steps: ArrayList<(Evolving<T>)-> T>): (Evolving<S>)-> T =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            coProcess(first / next, tail)
        }
    }

suspend fun <S,T> coProcess(first: (Evolving<S>)-> T, vararg steps:suspend (Evolving<T>)-> T): suspend (Evolving<S>)-> T =
    when(steps.isEmpty()) {
        true -> suspended(first)
        false -> {
            val next = steps.first()
            val tail = SuspendedFunctions(*steps).tail()
            coProcess(first / next, tail)
        }
    }
suspend fun <S,T> coProcess(first: suspend (Evolving<S>)-> T, vararg steps:suspend (Evolving<T>)-> T): suspend (Evolving<S>)-> T =
    when(steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = SuspendedFunctions(*steps).tail()
            coProcess(first / next, tail)
        }
    }
tailrec suspend fun <S,T> coProcess(first: suspend  (Evolving<S>)-> T, steps: SuspendedFunctions<Evolving<T>, T>): suspend (Evolving<S>)-> T =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            coProcess(first / next, tail)
        }
    }
