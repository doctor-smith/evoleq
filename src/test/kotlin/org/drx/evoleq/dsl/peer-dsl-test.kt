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

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.coroutines.DuplicatorMessage
import org.drx.evoleq.coroutines.onNext
import org.drx.evoleq.peer.Peer
import org.drx.evoleq.peer.connect
import org.drx.evoleq.peer.disconnect
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
            println("subscriber2: received message = $m")
        }

        peer.manager.send(DuplicatorMessage.Subscribe(Key1::class,subscriber1))
        peer.manager.send(DuplicatorMessage.Subscribe(Key2::class,subscriber2))

        delay(100)
        peer.input.send("1234567890")
        delay(100)
        peer.input.send("12345")
        delay(100)
        peer.manager.send(DuplicatorMessage.UnSubscribe(Key2::class))
        delay(100)
        peer.input.send("1234567890")

        delay(100)
        peer.manager.send(DuplicatorMessage.Terminate(peer.id))

        delay(100)
    }


    @Test fun connectAndDisconnectPeers() = runBlocking{
        var received = ""

        class Peer1
        val peer1 = peer<String, String, String>{
            val id = Peer1::class
            val scope = DefaultStubScope()
            stub(stub{
                id(id)
                evolve{ scope.parallel{
                    received = it
                    it
                } }
            })

            input(scope.receiver<String>()
                .onNext(scope){ s->
                    //println("input1: received message = $s")
                    stub.evolve(s)
                }
            )

            duplicator(scope.duplicator(owner = id))
        }
        class Peer2
        val peer2 = peer<String, String, String>{
            val id = Peer2::class
            val scope = DefaultStubScope()
            stub(stub{
                id(id)
                evolve{ scope.parallel{
                    duplicator.send(it)
                    it
                } }
            })

            input(scope.receiver<String>()
                .onNext(scope){ s->
                    //println("input2: received message = $s")
                    stub.evolve(s)
                }
            )

            duplicator(scope.duplicator(owner = id))
        }

        peer1.connect(peer2)
        delay(100)
        assert(peer1.subscriptions.contains(Peer2::class))
        val message1 = "Hello Peer1"
        peer2.input.send(message1)
        delay(100)
        assert(received == message1)
        peer1.disconnect(peer2)
        delay(100)
        val message2 = "Bye Peer1"
        peer2.input.send(message2)
        delay(100)
        assert(!peer1.subscriptions.contains(Peer2::class))
        assert(received == message1)

    }
}