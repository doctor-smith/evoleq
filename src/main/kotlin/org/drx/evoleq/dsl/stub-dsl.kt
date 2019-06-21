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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.LazyEvolving
import org.drx.evoleq.stub.*
import kotlin.reflect.KClass

@Suppress("FunctionName")
fun DefaultStubScope() :CoroutineScope =  CoroutineScope(SupervisorJob())

open class StubConfiguration<D>() : Configuration<Stub<D>> {

    constructor(scope: CoroutineScope): this(){
        this.scope  = scope
    }

    private var isLazyStub = false

    var id: KClass<*> = DefaultIdentificationKey::class

    protected var evolve: suspend (D)-> Evolving<D> = { d -> scope.immediate{d} }
    protected var lazyEvolving: LazyEvolving<D>? = null

    var scope : CoroutineScope = DefaultStubScope()
    /**
     * Collection of all sub-stubs
     */
     val stubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }
    /**
     * Stubs to be called by children
     */
    protected val parentalStubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }

    protected val parentalStubsMap: HashMap<KClass<*>, KClass<*>> by lazy { HashMap<KClass<*>, KClass<*>>() }
    /**
     * key: 'Key of a child class'
     * val: 'list of stub identifiers accessible to key-stub'
     */
    protected val crossChildAccessMap: HashMap<KClass<*>, ArrayList<KClass<*>>> by lazy{ HashMap<KClass<*>, ArrayList<KClass<*>>>() }
    /**
     * Shall child stub be visible to parent of the generated stub
     */
    //private val visibleToParent: ArrayList<KClass<*>> by lazy{ arrayListOf<KClass<*>>()}

    private lateinit var stub: Stub<D>

    init{

    }
    override fun configure(): Stub<D> {


        val stub: Stub<D> = if(isLazyStub){
             object : LazyStub<D> {
                override val scope: CoroutineScope
                    get() = this@StubConfiguration.scope

                override val id: KClass<*>
                    get() = this@StubConfiguration.id

                override suspend fun lazy(): LazyEvolving<D> = this@StubConfiguration.lazyEvolving!!

                override val stubs: HashMap<KClass<*>, Stub<*>>
                    get() = this@StubConfiguration.stubs
            }
        } else {
            object : Stub<D> {
                override val scope: CoroutineScope
                    get() = this@StubConfiguration.scope

                override val id: KClass<*>
                    get() = this@StubConfiguration.id

                override suspend fun evolve(d: D): Evolving<D> = this@StubConfiguration.evolve(d)

                override val stubs: HashMap<KClass<*>, Stub<*>>
                    get() = this@StubConfiguration.stubs
            }
        }
        parentalStubsMap.entries.forEach { entry ->
            val child = stub.stubs[entry.key]
            val parentStub = parentalStubs[entry.value]
            if (child != null && parentStub != null) {
                child.stubs[ParentStubKey::class] = parentStub
            }
        }
        this@StubConfiguration.stub = stub

        return stub
    }

    /**
     * Set the  id of the stub
     */
    fun id(id : KClass<*>) {
        this.id = id
    }
    /**
     * Set the  id of the stub
     */
    inline fun <reified ID> id() {
        this.id = ID::class
    }

    /**
     * Define the evolve function of the stub
     */
    open fun evolve( flow:suspend (D)-> Evolving<D> ) {
        this.evolve = flow
    }

    /**
     * Define the evolve function of the stub.
     * This approach guaranties structured concurrency even when
     * the scope of the stub is changed
     */
    open fun evolveLazy(lazyEvolving: LazyEvolving<D>) {
        this.lazyEvolving = lazyEvolving
        this.isLazyStub = true
    }

    /**
     * Access the stub provided by the parent during configuration.
     * E.g. within the evolve block of the child's dsl
     */
    @Suppress("unchecked_cast")
    fun <E> parent(): Stub<E> = stubs[ParentStubKey::class]!! as Stub<E>

    /**
     * Access a child during configuration.
     * E.g. within the evolve block
     */
    fun child(key : KClass<*>): Stub<*> = when(key == ParentStubKey::class) {
        true -> throw(Exception("accessing the parent is not allowed via child-function"))
        else -> stubs[key]!!
    }
    @Suppress("unchecked_cast")
    inline fun <reified K, E> child(): Stub<E> =when(K::class  == ParentStubKey::class) {
        true -> throw(Exception("accessing the parent is not allowed via child-function"))
        else -> stubs[K::class] as Stub<E>
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
    fun child( key: KClass<*>, stub: Stub<*>, accessedParentStub: KClass<*>? = null) = runBlocking {
        stubs[key] = stub
        if(accessedParentStub != null) {
            parentalStubsMap[key] = accessedParentStub
        }
    }

    /**
     * Using this function requires setting up id during configuration
     */
    fun child(stub: Stub<*>, accessedParentStub: KClass<*>? = null){
        child(stub.id, stub, accessedParentStub)
    }

    /**
     * Parental stubs are supposed to be called by
     * child-dsls via the the parent method
     */
    fun parentalStub(key: KClass<*>, stub: Stub<*>) {
        parentalStubs[key] = stub
    }

    /**
     * Define which siblings shall be accessible to child
     */
    fun accessibleSiblings(child: KClass<*>, vararg siblingKeys: KClass<*>) {
        crossChildAccessMap[child] = arrayListOf(*siblingKeys)
    }

    fun KClass<*>.grantAccessToSiblings(vararg siblingKeys: KClass<*>) = accessibleSiblings(this,*siblingKeys)

    /**
     * Using this method requires that the ids of the involved stubs have been set
     */
    fun accessibleSiblings(child: Stub<*>, vararg siblings: Stub<*>) {
        crossChildAccessMap[child.id] = arrayListOf(*siblings.map{stub -> stub.id}.toTypedArray())
    }

    /**
     * Using this method requires that the ids of the involved stubs ar set
     */
    fun Stub<*>.grantAccessToSiblings(vararg siblings: Stub<*>) = accessibleSiblings(this,*siblings)


    fun setupRelationsToChildren() {
        parentalStubsMap.entries.forEach { entry ->
            val child = stub.stubs[entry.key]
            val parentStub = parentalStubs[entry.value]
            if(child != null && parentStub != null) {
                child.stubs[ParentStubKey::class] = parentStub
            }
        }
    }



    fun whenReady(actOn: Stub<D>.()->Unit) = scope.parallel{
        while(!this@StubConfiguration::stub.isInitialized){
            delay(1)
        }
        //println(stub.stubs.keys)
        stub.actOn()
        stub
    }

    fun exposeConfiguration(): StubConfiguration<D> = this@StubConfiguration


    /**
     * Find a stub in this stub or in one of its children.
     */
    fun Stub<*>.find(key: KClass<*>): Stub<*>? = findByKey(key)

}

fun <D> stub(configuration: StubConfiguration<D>.()->Unit) : Stub<D> = configure(configuration)
fun <D> CoroutineScope.stub(configuration: StubConfiguration<D>.()->Unit) : Stub<D> = configure(this, configuration) as Stub<D>

