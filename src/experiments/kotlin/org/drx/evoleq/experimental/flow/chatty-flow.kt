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
package org.drx.evoleq.experimental.flow

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*
import org.drx.evoleq.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.flow.Evolver
import org.drx.evoleq.flow.Flow


abstract class ChattyFlow<D,T,I,O>(
    val conditions: EvolutionConditions<D, T>,
    val flow: (D)-> Evolving<D>,
    val initialMessage: I


) : Evolver<D>, TwoWayFlange<I, O> {

    val pipe: TwoWayPipe<I, O> by lazy { TwoWayPipe<I, O>() }
    init{
        input(initialMessage)
    }
    abstract fun chattyCheck(t:Pair<I,D>): Boolean

    abstract fun chattyUpdate(p: Pair<I,D>): O

    abstract fun waitForInput()

    override suspend fun evolve(data: D): Evolving<D> = Immediate {
        evolve(
            initialData = data,
            conditions = conditions
        ) { data ->
            runBlocking {
                waitForInput()
            }
            val input: I = pipe.oi().output().value
            val newData = when (chattyCheck(Pair(input, data))) {
                false -> Immediate { data }
                true -> flow(data)
            }
            runBlocking {
                chattyUpdate(Pair(input, newData.get()))
            }
            newData
        }
    }


    override fun input(stuff: I) {
        pipe.io().input(stuff)
    }

    override fun output(): SimpleObjectProperty<O> {
        return pipe.io().output()
    }
}



open class ChattyFlowBase<D,T,I,O> (
    private val chattyConditions: EvolutionConditions<TwoWayFlangedData<O, I, D>, TwoWayFlangedData<O, I, T>>,
    private val chattyFlow: (TwoWayFlangedData<O, I, D>)-> Evolving<TwoWayFlangedData<O, I, D>>,
    private val pipe: TwoWayPipe<I, O>
) : Flow<TwoWayFlangedData<O, I, D>, TwoWayFlangedData<O, I, T>>(chattyConditions, chattyFlow),
    TwoWayFlange<I, O> {
    override fun input(stuff: I) {
        pipe.io().input(stuff)
    }
    override fun output(): SimpleObjectProperty<O> {
        return pipe.io().output()
    }
/*
    override suspend fun evolve(math: TwoWayFlangedData<O, I, D>): Evolving<TwoWayFlangedData<O, I, D>> {
        return super.evolve(math)
    }
    */
}
data class TwoWayFlangedData<I,O,D>(val flange: TwoWayFlange<I, O>, val data: D)

class OneWayPipe<S>{
    private val input: SimpleObjectProperty<S> by lazy { SimpleObjectProperty<S>() }
    private val output: SimpleObjectProperty<S> by lazy { SimpleObjectProperty<S>() }
    init{
        output.bind(input)
    }
    fun input(stuff: S){
        input.value = stuff
    }
    fun output(): SimpleObjectProperty<S> = output
}
interface TwoWayFlange<I,O> {
    fun input(stuff: I)
    fun output(): SimpleObjectProperty<O>
}
interface TwoWayFlanges<I,O> {
    fun io(): TwoWayFlange<I, O>
    fun oi(): TwoWayFlange<O, I>
}
class TwoWayPipe<I,O>(
    private val io: OneWayPipe<I> = OneWayPipe(),
    private val oi: OneWayPipe<O> = OneWayPipe()
): TwoWayFlanges<I, O> {
    override fun io(): TwoWayFlange<I, O> = object:
        TwoWayFlange<I, O> {
        override fun input(stuff: I) = io.input(stuff)
        override fun output(): SimpleObjectProperty<O> = oi.output()
    }
    override fun oi(): TwoWayFlange<O, I> = object:
        TwoWayFlange<O, I> {
        override fun input(stuff: O) = oi.input(stuff)
        override fun output(): SimpleObjectProperty<I> = io.output()
    }
}



