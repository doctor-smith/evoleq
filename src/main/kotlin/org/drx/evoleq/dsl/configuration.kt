/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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

import org.drx.evoleq.evolving.Evolving
import kotlin.reflect.full.createInstance

interface Configuration<out D>
{
    fun configure() : D
}

open class ConfigurationConfiguration<D> : Configuration<Configuration<D>> {

    var conf: Configuration<D>? = null

    override fun configure(): Configuration<D> = conf!!
}
inline fun <D, reified C: Configuration<D>> configure(noinline sideEffect:  C.()->Unit) : D {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configure()
}


inline fun <D, reified C: Configuration<D>> setupConfiguration(noinline sideEffect:  C.()->Unit) : C {
    val c = C::class.createInstance()
    c.sideEffect()
    return c
}

inline fun <reified C> C.reconfigure(block:C.()->C): C {
    return this.block()
}




interface SuspendedConfiguration<out D> {
    suspend  fun configure(): Evolving<D>
}
suspend inline fun <D, reified C: SuspendedConfiguration<D>> configureSuspended(noinline  sideEffect: suspend C.()->Unit) : Evolving<D> {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configure()
}