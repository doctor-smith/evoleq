package org.drx.evoleq.dsl

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.onNext
import org.drx.evoleq.peer.Peer
import org.drx.evoleq.stub.Key1
import org.drx.evoleq.stub.Key2
import org.junit.Test

class PeerDslTest {

    @Test fun peerDslBasics() = runBlocking {

        val peer = peer<Int, String, Boolean>{
            val id = Peer::class
            val scope = DefaultStubScope()
            stub(stub{
                id(id)
                evolve{
                    scope.parallel {
                        duplicator.send(it >= 10)
                        it
                    }
                }
            })

            input(scope.receiver<String>()
                .onNext(scope){ s->
                    println("input: received message = $s")
                    stub.evolve(s.length)
                }
            )

            duplicator(scope.duplicator(owner = id))
        }
        val scope1 = DefaultStubScope()
        val subscriber1 = scope1.receiver<Boolean>()
            .onNext(scope1){ m ->
                println("subscriber1: received message = $m")
            }

        val scope2 = DefaultStubScope()
        val subscriber2 = scope2.receiver<Boolean>().onNext(scope1){ m ->
            println("subscriber1: received message = $m")
        }

        peer.manager.send(DuplicatorMessage.Subscribe(Key1::class,subscriber1))
        peer.manager.send(DuplicatorMessage.Subscribe(Key2::class,subscriber2))

        delay(100)
        peer.input.send("1234567890")
        peer.input.send("12345")
        peer.input.send("1234567890")

        delay(100)
        peer.manager.send(DuplicatorMessage.Terminate(peer.id))

        delay(100)
    }

}