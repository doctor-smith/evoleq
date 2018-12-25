/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.examples.application

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import kotlin.reflect.KClass

interface Stub<D> {
    suspend fun stub(d: D): Evolving<D>
    fun stubs(): HashMap<KClass<*>, Stub<*>>
}
class InitStub<D> : Stub<D>{
    override suspend fun stub(d: D): Evolving<D> {
        return Immediate{d}
    }
    val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
    override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs
}