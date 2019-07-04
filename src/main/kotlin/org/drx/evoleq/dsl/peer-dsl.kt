package org.drx.evoleq.dsl

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.coroutines.Duplicator
import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.peer.Peer
import org.drx.evoleq.stub.Stub
import kotlin.reflect.KClass

open class PeerConfiguration<D, I, O> : Configuration<Peer<D, I, O>> {

    lateinit var stub: Stub<D>
    lateinit var receiver: Receiver<I>
    lateinit var duplicator: Duplicator<O>

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
        override suspend fun evolve(d: D): Evolving<D> = stub.evolve(d)
    }
}

fun <D,I,O> peer(configuration: PeerConfiguration<D,I,O>.()->Unit): Peer<D,I,O> = configure(configuration)