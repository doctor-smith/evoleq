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

import org.drx.evoleq.stub.ID

sealed class Output<out Input,out Data>{
    data class Input<Input>(val input: suspend (Input)->Unit) : Output<Input, Nothing>()
    data class Update<Data, Part>(val update: suspend (suspend Part.()->Part)->Unit): Output<Nothing,Data>()
}

class Outputs<Input,Data> : HashMap<ID, Output<Input, Data>>() {
    fun<I : Input> input(id: ID,input: suspend (I)->Unit) {
        this[id] = Output.Input(input)
    }

    fun <Part> update(id: ID, update: suspend (suspend Part.()->Part)->Unit) {
        this[id] = Output.Update(update)
    }
}

fun <Input, Data> outputs(configure: Outputs<Input, Data>.()->Outputs<Input,Data>): Outputs<Input,Data> = with(Outputs<Input,Data>()) {
    configure()
    this
}


suspend fun <Data, Part> Outputs<Nothing, Data>.update(id: ID, function: suspend Part.()->Part) = with(get(id)) {
    try{ (this as Output.Update<Data, Part>).update(function) } catch(ignored : Exception){}
}


suspend fun <Input> Outputs<Input, Nothing>.input(id: ID, input: Input) = with(get(id)) {
    try{ (this as Output.Input<Input>).input(input) } catch(ignored : Exception){}
}