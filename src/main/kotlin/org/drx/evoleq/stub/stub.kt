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
package org.drx.evoleq.stub

import kotlinx.coroutines.CoroutineScope
import org.drx.evoleq.annotation.Experimental
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.LazyEvolving
import org.drx.evoleq.flow.Evolver
import org.drx.evoleq.flow.LazyFlow
import org.drx.evoleq.flow.SuspendedFlow
import kotlin.reflect.KClass

typealias ID = KClass<*>

class DefaultIdentificationKey
interface Stub<D> : Evolver<D> {

    val id: KClass<*>
    val stubs: HashMap<KClass<*>, Stub<*>>

    override suspend fun evolve(d: D): Evolving<D> = scope.parallel{ d }

}

/**
 * Fix stub-functionality, run on different scopes
 */
@Experimental
interface LazyStub<D> : Stub<D> {
    suspend fun lazy(): LazyEvolving<D>
    override suspend fun evolve(d: D): Evolving<D> = lazy()(scope,d)
}



class ParentStubKey

fun <D,T> Stub<D>.toFlow(conditions: EvolutionConditions<D,T>): SuspendedFlow<D,T> = suspendedFlow {
    scope(this@toFlow.scope)
    conditions(conditions)
    flow{ d -> this@toFlow.evolve(d) }
}
@Experimental
fun <D,T> LazyStub<D>.toLazyFlow(conditions: EvolutionConditions<D,T>): suspend CoroutineScope.()-> Evolver<D> = { LazyFlow(conditions,lazy()) }
@Experimental
fun <D> lazyStub(stub: Stub<D>): LazyStub<D> = stub as LazyStub<D>
@Experimental
fun <D> Stub<D>.asLazyStub(): LazyStub<D> = this as LazyStub<D>
@Experimental
fun <D> asLazy(stub: Stub<D>): LazyStub<D> = stub as LazyStub<D>
@Experimental
fun <D> toLazy(stub: Stub<D>): LazyStub<D> = stub as LazyStub<D>
@Experimental
fun <D> lazyfy(stub: Stub<D>): LazyStub<D> = stub as LazyStub<D>
@Experimental
fun <D> free(stub: Stub<D>): LazyStub<D> = stub as LazyStub<D>


/**
 * Find stub by key.
 */
@Suppress("unchecked_cast")
fun Stub<*>.findByKey(key: KClass<*>): Stub<*>? {
    this.stubs.forEach{
        if(it.key == key){
            return it.value
        }
    }
    this.stubs.values.forEach{
        val stub = it.findByKey(key)
        if(stub != null){
            return stub
        }
    }
    return null
}

/**
 * Find stub by id and return typed result
 */
@Suppress("unchecked_cast")
fun <D> Stub<*>.find(id: ID): Stub<D>? {
    this.stubs.forEach{
        if(it.key == id){
            return try{
                it.value as Stub<D>
            } catch(ignored : Throwable) {
                null
            }
        }
    }
    this.stubs.values.forEach{
        val stub = it.find<D>(id)
        if(stub != null){
            return stub
        }
    }
    return null
}

@Suppress("unchecked_cast")
fun <D> Stub<*>.data(): Stub<D> = this as Stub<D>
