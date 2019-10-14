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
import org.drx.evoleq.coroutines.Duplicator
import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.peer.Peer
import org.drx.evoleq.stub.ID
import org.drx.evoleq.stub.Stub
import kotlin.reflect.KClass

open class PeerConfiguration<D, I, O> : Configuration<Peer<D, I, O>> {

    lateinit var stub: Stub<D>
    lateinit var receiver: Receiver<I>
    lateinit var duplicator: Duplicator<O>

    val subscriptionList = arrayListOf<ID>()

    fun stub(stub: Stub<D>){ this.stub = stub }

    fun input(receiver: Receiver<I>){ this.receiver = receiver }

    fun duplicator(duplicator: Duplicator<O>){ this.duplicator = duplicator }

    override fun configure(): Peer<D, I, O> = object: Peer<D,I,O> {
        override val input: Receiver<I>
            get() = receiver
        override val manager: Receiver<DuplicatorMessage<O>>
            get() = duplicator.subscriptionPort
        override val id: KClass<*>
            get() = stub.id
        override val stubs: HashMap<KClass<*>, Stub<*>>
            get() = stub.stubs
        override val scope: CoroutineScope
            get() = stub.scope
        override val subscriptions: ArrayList<ID>
            get() = subscriptionList
        override suspend fun evolve(d: D): Evolving<D> = stub.evolve(d)


    }
}

fun <D,I,O> peer(configuration: PeerConfiguration<D,I,O>.()->Unit): Peer<D,I,O> = configure(configuration)

/*
class GateWay
open class PeerPoolConfiguration<P,I,O> : Configuration<PeerPool<P, I, O>> {
    lateinit var peers: ArrayList<Peer<Any?, P,P>>
    lateinit var peer: Peer<P,I,O>

    val gateway: Stub<GateWayMessage> = stub{
        id(GateWay::class)
        evolve{message ->
            when(message){
                is GateWayMessage.Empty -> Immediate{GateWayMessage.Empty}
                is GateWayMessage.Typed.Send<*> -> Parallel{
                    GateWayMessage.Typed.Send(message.sender,message.receiver,peers.find{message.receiver == it.id }!!.evolve(message.data))
                }
                is GateWayMessage.Typed.Sent<*> ->Parallel{message}
            }
        }
    }

    fun peer(configuration: PeerConfiguration<P,I,O>.()->Unit){
        peer = org.drx.evoleq.dsl.peer(configuration)
    }



    override fun configure(): PeerPool<P, I, O> = object: PeerPool<P,I,O>{
        override val input: Receiver<I>
            get() = peer.input
        override val manager: Receiver<DuplicatorMessage<O>>
            get() = peer.manager
        override val id: KClass<*>
            get() = peer.id
        override val stubs: HashMap<KClass<*>, Stub<*>>
            get() = peer.stubs
        override val scope: CoroutineScope
            get() = peer.scope
        override val subscriptions: ArrayList<ID>
            get() = peer.subscriptions
        override suspend fun evolve(d: P): Evolving<P> = peer.evolve(d)

        override val peers: ArrayList<Peer<Any?, P, P>>
            get() = this@PeerPoolConfiguration.peers
        override val gateway: Receiver<GateWayMessage>
            get() = this@PeerPoolConfiguration.gateway
    }
}

 */