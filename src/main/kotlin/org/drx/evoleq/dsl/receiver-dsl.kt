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
import kotlinx.coroutines.channels.actor
import org.drx.evoleq.coroutines.BaseReceiver
import org.drx.evoleq.coroutines.Duplicator
import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.stub.ID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


fun <D> CoroutineScope.receiver(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = 0,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCompletion: CompletionHandler? = null,
    block: suspend ActorScope<D>.(D) -> Unit = {}
): BaseReceiver<D> {
    val c: Channel<D> = Channel()
    val actor = this@receiver.actor<D>(context, capacity, start, onCompletion){
        for(d in channel){
            c.send(d)
            block(d)
        }
    }

    return BaseReceiver(actor,c)
}



fun <D> CoroutineScope.duplicator(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = 0,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    owner: ID? = null,
    onCompletion: CompletionHandler? = null
): Duplicator<D> {
    val receivers = HashMap<ID, Receiver<D>>()
    val actor = this@duplicator.actor<D>(context, capacity, start, onCompletion){

        for(d in channel){
            receivers.values.forEach{it.send(d)}
        }
    }
    val subscriptionPort = receiver<DuplicatorMessage<D>> {}

    return Duplicator(actor,subscriptionPort,receivers, owner)
}
