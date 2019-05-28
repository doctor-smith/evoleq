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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.receivingStub
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.stub.ID
import org.drx.evoleq.stub.toFlow

interface Receiver<in D>{
    suspend fun send(data: D): Unit
}

open class BaseReceiver<D>(open val actor: SendChannel<D>, open val channel: Channel<D>) : Receiver<D>{
    override suspend fun send(data: D) = actor.send(data)
}
open class InputAdapter<in I, D>(open val receiver: Receiver<D>,open  val transform: (I)->D): Receiver<I> {
    override suspend fun send(data: I) = receiver.send(transform(data))
}

infix fun <I, D> Receiver<D>.input(transform: (I)->D): InputAdapter<I, D> = InputAdapter(this){ input ->transform(input) }
fun <D> Receiver<D>.input(): InputAdapter<D,D> = input{ d -> d }

sealed class DuplicatorMessage<D> {
    class Wait<D> : DuplicatorMessage<D>()
    data class Subscribe<D>(val id: ID, val receiver: Receiver<D>) : DuplicatorMessage<D>()
    data class UnSubscribe<D>(val id: ID) : DuplicatorMessage<D>()
    class Terminate<D> : DuplicatorMessage<D>()
}

/**
 * Duplicate incoming messages
 */
class Duplicator<D>(
    override val actor: SendChannel<D>,
    val subscriptionPort: BaseReceiver<DuplicatorMessage<D>>,
    val receivers: HashMap<ID, Receiver<D>> = HashMap()
) : BaseReceiver<D>(actor,Channel()){

    private val manager = receivingStub<DuplicatorMessage<D>, DuplicatorMessage<D>> {
        gap{
            from{ Immediate{it} }
            to{ _, message -> Immediate{message} }
        }
        receiver(subscriptionPort)
    }
    private val managerStub = stub<DuplicatorMessage<D>>{
        id(Duplicator::class)
        evolve{ message -> when(message) {
            is DuplicatorMessage.Subscribe -> Parallel{
                receivers[message.id] = message.receiver
                DuplicatorMessage.Wait()
            }
            is DuplicatorMessage.UnSubscribe -> Parallel{
                receivers.remove(message.id)
                DuplicatorMessage.Wait()
            }
            is DuplicatorMessage.Wait -> manager.evolve(message)
            is DuplicatorMessage.Terminate -> Immediate{ DuplicatorMessage.Terminate<D>() }
        } }
    }
    private val managerFlow = managerStub.toFlow<DuplicatorMessage<D>, Boolean>(conditions{
        testObject(true)
        check{b->b}
        updateCondition { message -> message !is DuplicatorMessage.Terminate<D> }
    })

    init{
        Parallel<Unit> {
            val message = managerFlow.evolve(DuplicatorMessage.Wait()).get()
            assert(message is DuplicatorMessage.Terminate)
        }

    }
}
