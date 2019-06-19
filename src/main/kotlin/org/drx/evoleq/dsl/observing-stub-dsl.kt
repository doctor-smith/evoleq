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

import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.drx.evoleq.conditions.once
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.flow.enter
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.fill
import org.drx.evoleq.message.Message
import org.drx.evoleq.stub.Stub
import org.drx.evoleq.time.WaitForProperty

class ObservePropertyStub
sealed class ObservePropertyMessage : Message {
    object Observe : ObservePropertyMessage()
    object Pause : ObservePropertyMessage()
}
open class ObservingStubConfiguration<D,P>() : StubConfiguration<D>() {


    constructor(scope: CoroutineScope) : this(){
        this.scope = scope
    }

    //var scope: CoroutineScope = GlobalScope

    private var preEvolve: suspend (D)-> Evolving<D> = suspended{ d: D -> Immediate{d} }
    private val stack: ArrayList<P> by lazy{ arrayListOf<P>() }
    private val stackEmpty: SimpleObjectProperty<Boolean> by lazy { SimpleObjectProperty<Boolean>(true) }

    private var gap: Gap<D, P>? = null


    private var observedProperty: Property<P>? = null

    /**
     * Observe a property
     */
    fun observe(property: Property<P>) {
        observedProperty = property
    }

    /**
     * The gap to close
     */
    fun gap(configuration: GapConfiguration<D, P>.()->Unit) {
        this.gap = configure(configuration)
    }

    /**
     * Define the pre-evolve function of the stub.
     * In principle it is not necessary to use this function during usage
     */
    override fun evolve( flow:suspend (D)-> Evolving<D>) {
        this.preEvolve = flow
    }

    override fun configure(): Stub<D> {
        // add new values to stack
        val observing = SimpleBooleanProperty(true)
        val listener = ChangeListener<P>{ _, _, newValue ->
            if(observing.value) {
                //println("adding $newValue to stack")
                stack.add(newValue)
                stackEmpty.value = false
            }
            else{
                //println("observing = false")
            }
        }

        observedProperty!!.addListener(listener)

        super.stubs[ObservePropertyStub::class] = stub<ObservePropertyMessage>{
            evolve { message ->
                when (message) {
                    is ObservePropertyMessage.Observe -> scope.parallel {
                        observing.value = true
                        message
                    }
                    is ObservePropertyMessage.Pause -> scope.parallel {
                        observing.value = false
                        message
                    }
                }
            }
        }


        // configure evolution function
        var setupDone: Boolean = false
        scope.launch {
            // view evolution function as
            // flow and make it enter the gap
            val flowGap = suspendedFlow<D,Boolean> {
                conditions(once())
                flow { d:D ->preEvolve(d) }
            }.enter(gap!!)
            // define the side-effect.
            // Have to distinguish the cases of empty and non-empty stack
            val sideEffect = suspended { p:P -> Parallel<P> {
                //println(stack.size)
                when (stack.isEmpty()) {
                    true -> {
                        val x = WaitForProperty(stackEmpty).toChange().get()
                        val first = stack.first()
                        stack.removeAt(0)
                        if (stack.isEmpty()) {
                            stackEmpty.value = true
                        }
                        first
                    }
                    false -> {
                        val first = stack.first()
                        stack.removeAt(0)
                        if (stack.isEmpty()) {
                            stackEmpty.value = true
                        }
                        first
                    }
                }
            }
            }
            super.evolve = suspended( flowGap.fill( sideEffect ))
            setupDone = true
        }
        // await configuration to be done
        while(!setupDone) {
            Thread.sleep(0,1)
        }
        return  super.configure()

    }
}

/**
 * Configure an observing stub
 * Important: The stub will not work unless a gap is defined
 */
fun <D,P> observingStub(configuration: ObservingStubConfiguration<D,P>.()->Unit) : Stub<D> = configure(configuration)
fun <D,P> CoroutineScope.observingStub(configuration: ObservingStubConfiguration<D,P>.()->Unit) : Stub<D> = configure(this,configuration)
