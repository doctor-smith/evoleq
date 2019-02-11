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

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.GlobalScope
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
import org.drx.evoleq.stub.DefaultIdentificationKey
import org.drx.evoleq.stub.ParentStubKey
import org.drx.evoleq.stub.Stub
import org.drx.evoleq.time.WaitForProperty
import java.lang.Exception
import kotlin.reflect.KClass

open class StubConfiguration<D> : Configuration<Stub<D>> {

    private var id: KClass<*> = DefaultIdentificationKey::class

    protected var evolve: suspend (D)-> Evolving<D> = { d -> Immediate{d} }
    /**
     * Collection of all sub-stubs
     */
    protected val stubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }
    /**
     * Stubs to be called by children
     */
    private val parentalStubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }

    private val parentalStubsMap: HashMap<KClass<*>, KClass<*>> by lazy { HashMap<KClass<*>, KClass<*>>() }
    /**
     * key: 'Key of a child class'
     * val: 'list of stub identifiers accessible to key-stub'
     */
    private val crossChildAccessMap: HashMap<KClass<*>, ArrayList<KClass<*>>> by lazy{ HashMap<KClass<*>, ArrayList<KClass<*>>>() }
    /**
     * Shall child stub be visible to parent of the generated stub
     */
    //private val visibleToParent: ArrayList<KClass<*>> by lazy{ arrayListOf<KClass<*>>()}


    override fun configure(): Stub<D> {
        val stub = object : Stub<D> {

            override val id: KClass<*>
                get() = this@StubConfiguration.id

            override suspend fun evolve(d: D): Evolving<D> = this@StubConfiguration.evolve(d)

            override val stubs: HashMap<KClass<*>, Stub<*>>
                get() = this@StubConfiguration.stubs
        }

        parentalStubsMap.entries.forEach { entry ->
            val child = stub.stubs[entry.key]
            val parentStub = parentalStubs[entry.value]
            if(child != null && parentStub != null) {
                child.stubs[ParentStubKey::class] = parentStub
            }
        }

        return stub
    }

    /**
     * define the evolve function of the stub
     */
    open fun evolve( flow:suspend (D)-> Evolving<D> ) {
        this.evolve = flow
    }

    fun id(id : KClass<*>) {
        this.id = id
    }


    /**
     * Access the stub provided by the parent during configuration.
     * E.g. within the evolve block of the child's dsl
     */
    fun parent(): Stub<*> = stubs[ParentStubKey::class]!!

    /**
     * Access a child during configuration.
     * E.g. within the evolve block
     */
    fun child(key : KClass<*>): Stub<*> = when(key == ParentStubKey::class) {
        true -> throw(Exception("accessing the parent is not allowed via child-function"))
        else -> stubs[key]!!
    }

    /**
     * Access a sibling during configuration
     */
    fun sibling(childKey: KClass<*>, siblingKey: KClass<*>): Stub<*>? = when(crossChildAccessMap[childKey] != null){
        true -> when(crossChildAccessMap[childKey]!!.contains(siblingKey)) {
            true -> child(siblingKey)
            else -> null
        }
        false -> null
    }
    //fun Stub<*>.sibling(siblingKey: KClass<*>): Stub<*>? = sibling(this.id, siblingKey)


    /**
     * Configure a child stub
     */
    fun child( key: KClass<*>, stub: Stub<*>, accessedParentStub: KClass<*>? = null) {
        stubs[key] = stub
        if(accessedParentStub != null) {
            parentalStubsMap[key] = accessedParentStub
        }
    }

    /**
     * Using this function requires setting up id during configuration
     */
    fun child(stub: Stub<*>, accessedParentStub: KClass<*>?? = null){
        child(stub.id, stub, accessedParentStub)
    }

    /**
     * Parental stubs are supposed to be called by
     * child-dsls via the the parent method
     */
    fun parentalStub(key: KClass<*>, stub: Stub<*>) {
        parentalStubs[key] = stub
    }

    fun accessibleSiblings(child: KClass<*>, vararg siblingKeys: KClass<*>) {
        crossChildAccessMap[child] = arrayListOf(*siblingKeys)
    }

    fun KClass<*>.grantAccessToSiblings(vararg siblingKeys: KClass<*>) = accessibleSiblings(this,*siblingKeys)

    /**
     * Using this method requires that the ids of the involved stubs ar set
     */
    fun accessibleSiblings(child: Stub<*>, vararg siblings: Stub<*>) {
        crossChildAccessMap[child.id] = arrayListOf(*siblings.map{stub -> stub.id}.toTypedArray())
    }

    /**
     * Using this method requires that the ids of the involved stubs ar set
     */
    fun Stub<*>.grantAccessToSiblings(vararg siblings: Stub<*>) = accessibleSiblings(this,*siblings)

/*
    fun find(key: KClass<*>): Stub<*>? {
        stubs.forEach{
            if(it.key == key){
                return it.value
            }
        }
        stubs.values.forEach{
            if(it.stubs)
        }
        return null
    }
*/
}

fun <D> stub(configuration: StubConfiguration<D>.()->Unit) : Stub<D> = configure(configuration)

class ObservePropertyStub
sealed class ObservePropertyMessage : Message {
    object Observe : ObservePropertyMessage()
    object Pause : ObservePropertyMessage()
}
open class ObservingStubConfiguration<D,P> : StubConfiguration<D>() {

    private var preEvolve: suspend (D)-> Evolving<D> = suspended{d: D -> Immediate{d}}
    private val stack: ArrayList<P> by lazy{ arrayListOf<P>() }
    private val stackEmpty: SimpleObjectProperty<Boolean> by lazy { SimpleObjectProperty<Boolean>(true) }
    private var observedProperty: SimpleObjectProperty<P>? = null
    private var gap: Gap<D, P>? = null

    /**
     * Observe a property
     */
    fun observe(property: SimpleObjectProperty<P>) {
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
    override fun evolve( flow:suspend (D)-> Evolving<D> ) {
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
                    is ObservePropertyMessage.Observe -> Parallel {
                        observing.value = true
                        message
                    }
                    is ObservePropertyMessage.Pause -> Parallel {
                        observing.value = false
                        message
                    }
                }
            }
        }


        // configure evolution function
        var setupDone: Boolean = false
        GlobalScope.launch {
            // view evolution function as
            // flow and make it enter the gap
            val flowGap = suspendedFlow<D,Boolean> {
                conditions(once())
                flow { d:D ->preEvolve(d) }
            }.enter(gap!!)
            // define the side-effect.
            // Have to distinguish the cases of empty and non-empty stack
            val sideEffect = suspended { p:P -> Parallel {
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
            }}
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

fun <D,P> observingStub(configuration: ObservingStubConfiguration<D,P>.()->Unit) : Stub<D> = configure(configuration)



