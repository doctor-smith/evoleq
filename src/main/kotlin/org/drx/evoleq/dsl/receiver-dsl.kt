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

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Receiver<D>(val actor: SendChannel<D>, val channel: Channel<D>){
    suspend fun send(data: D) = actor.send(data)
}


suspend fun <D> CoroutineScope.receiver(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = 0,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCompletion: CompletionHandler? = null,
    block: suspend ActorScope<D>.(D) -> Unit = {}
): Receiver<D> {
    val c: Channel<D> = Channel()
    val actor = actor<D>(context, capacity, start, onCompletion){
        for(d in channel){
            c.send(d)
            block(d)
        }
    }

    return Receiver(actor,c)
}
