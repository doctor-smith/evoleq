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
package org.drx.evoleq.peer

import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.stub.ID
import org.drx.evoleq.stub.Stub

interface Peer<D, I, O> : Stub<D>{
    /**
     * Receive input of type I
     * [Receiver]
     */
    val input: Receiver<I>
    /**
     * Manage peer;
     * Receive duplicator-messages - output type O
     * [Receiver], [DuplicatorMessage]
     */
    val manager: Receiver<DuplicatorMessage<O>>
    /**
     * Store subscriptions to other duplicators
     */
    val subscriptions: ArrayList<ID>
}

suspend fun <D,E, I, O, J> Peer<D,I,O>.connect(peer: Peer<E, J, I>): Peer<D,I,O> {
    peer.manager.send(DuplicatorMessage.Subscribe(id, input))
    subscriptions.add(peer.id)
    return this
}
suspend fun <D,E, I,O, J> Peer<D,I,O>.disconnect(peer: Peer<E, J, I>): Peer<D,I,O> {
    peer.manager.send(DuplicatorMessage.UnSubscribe(id))
    subscriptions.remove(peer.id)
    return this
}

interface PeerPool<P, I, O> : Peer<P, I, O>{
    val peers: ArrayList<Peer<Any?,P,P>>
    val gateway: Receiver<GateWayMessage>
}

sealed class GateWayMessage{
    object Empty : GateWayMessage()
    sealed class Typed<P> : GateWayMessage() {
        data class Send<P>(val receiver: ID,val sender: ID, val data: P) : Typed<P>()
        data class Sent<P>(val receiver: ID, val data: P) : Typed<P>()
    }
}

fun <P,I,O> PeerPool<P,I,O>.register(peer: Peer<Any?,P,P>): PeerPool<P,I,O> {
    peers.add(peer)
    peer.stubs[id] = stub<GateWayMessage.Typed<P>>{
        id(id)
        evolve{
            when(it){
                is GateWayMessage.Typed.Send -> {
                    gateway.send(it)
                    peer.scope.parallel{GateWayMessage.Typed.Sent(it.receiver, it.data)}
                }
                is GateWayMessage.Typed.Sent -> {
                    peer.scope.parallel{it}
                }
            }
        }
    }
    return this
}

