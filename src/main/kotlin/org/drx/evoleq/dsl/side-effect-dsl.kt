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
import org.drx.evoleq.sideeffect.InitialSideEffect
import org.drx.evoleq.sideeffect.TerminalSideEffect
import org.drx.evoleq.sideeffect.TotalSideEffect

fun<T> terminalSideEffect(sideEffect: (T)->Unit): TerminalSideEffect<T> = object : TerminalSideEffect<T>() {
    override fun invoke(t: T) = sideEffect(t)
}


fun<T> initialSideEffect(sideEffect: ()->T): InitialSideEffect<T> = object : InitialSideEffect<T>() {
    override fun invoke() = sideEffect()
}
fun <T> CoroutineScope.initialSideEffect(sideEffect: CoroutineScope.()->T): CoroutineScope.()->InitialSideEffect<T> = { object : InitialSideEffect<T>() {
    override fun invoke() = this@initialSideEffect.sideEffect()
} }

fun<T> totalSideEffect(sideEffect: ()->Unit): TotalSideEffect = object : TotalSideEffect() {
    override fun invoke() = sideEffect()
}