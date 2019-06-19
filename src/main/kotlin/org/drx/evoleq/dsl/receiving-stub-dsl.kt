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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drx.evoleq.conditions.once
import org.drx.evoleq.coroutines.BaseReceiver
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.flow.enter
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.stub.Stub


class ReceivingStub
sealed class ReceivingStubMessage {
    sealed class Request : ReceivingStubMessage() {
        object Observe : Request()
        object Ignore : Request()
        object Close : Request()
    }
    sealed class Response: ReceivingStubMessage() {
        object Observing : Response()
        object Ignoring : Response()
        object Closed : Response()
    }
    object Empty: ReceivingStubMessage()
}

/**
 * ReceivingStub configuration
 */
open class ReceivingStubConfiguration<W, P>() : StubConfiguration<W>() {

    constructor(scope: CoroutineScope) : this(){
        this.scope = scope
    }

    //var scope: CoroutineScope = GlobalScope

    private var preEvolve: suspend (W)-> Evolving<W> = suspended{ d: W -> scope.immediate{d}}
    lateinit var gap: Gap<W, P>
    lateinit var receiver: BaseReceiver<P>
    /**
     * The gap to close
     */
    fun gap(configuration: GapConfiguration<W, P>.()->Unit) {
        this.gap = configure(configuration)
    }

    fun receiver(receiver: BaseReceiver<P>) {
        this.receiver = receiver
    }

    /**
     * Define the pre-evolve function of the stub.
     * In principle it is not necessary to use this function during usage
     */
    override fun evolve( flow:suspend (W)-> Evolving<W>) {
        this.preEvolve = flow
    }

    override fun configure(): Stub<W> {
        var setupDone = false
        scope.launch {
            var observing = true
            val stack = arrayListOf<P>()
            parallel<Unit> {
                for(p in receiver.channel) {
                    if (observing) {
                        //println("added $p to stack")
                        stack.add(p)
                    }
                }
            }

            val flowGap = suspendedFlow<W,Boolean> {
                conditions(once())
                flow { d:W ->preEvolve(d) }
            }.enter(gap)
            val sideEffect = suspended { p: P ->
                parallel<P> {
                    if(!observing) {
                        stack.clear()
                    }
                    while(stack.isEmpty()){
                        delay(1)
                    }
                    val pNew = stack.first()
                    stack.removeAt(0)
                    pNew
                }
            }

            super.stubs[ReceivingStub::class] = stub<ReceivingStubMessage>{
                id(ReceivingStub::class)
                evolve{
                        message -> when (message){
                    is ReceivingStubMessage.Request -> when(message){
                        is ReceivingStubMessage.Request.Observe -> parallel{
                            observing = true
                            ReceivingStubMessage.Response.Observing
                        }
                        is ReceivingStubMessage.Request.Ignore -> parallel{
                            observing = false
                            ReceivingStubMessage.Response.Ignoring
                        }
                        is ReceivingStubMessage.Request.Close -> parallel{
                            try {
                                receiver.actor.close()
                            }catch(exception: Exception){}
                            ReceivingStubMessage.Response.Closed
                        }
                    }
                    else -> Parallel{ReceivingStubMessage.Empty}
                }
                } }

            super.evolve = suspended(flowGap.fill(sideEffect))
            setupDone = true
        }
        // await configuration to be done
        while(!setupDone) {
            Thread.sleep(0,1)
        }
        return super.configure()
    }
}

/*.
 * Configure a receiving stub
 */
fun <W,P> receivingStub(configuration : ReceivingStubConfiguration<W,P>.()->Unit): Stub<W> = configure(configuration)

fun <W,P> CoroutineScope.receivingStub(configuration : ReceivingStubConfiguration<W,P>.()->Unit): Stub<W> = configure(this,configuration)
