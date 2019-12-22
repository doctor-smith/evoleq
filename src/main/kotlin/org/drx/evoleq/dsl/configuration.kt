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

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Phase
import kotlin.reflect.full.createInstance

interface Configuration<out D>
{
    fun configure() : D

    suspend fun configureSuspended(): D = configure()
}

interface ConfigurationPhase : Phase

open class ConfigurationConfiguration<D> : Configuration<Configuration<D>> {

    var conf: Configuration<D>? = null

    override fun configure(): Configuration<D> = conf!!
}

inline fun <D, reified C: Configuration<D>> configure(noinline sideEffect:  C.()->Unit) : D {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configure()
}
suspend inline fun <D, reified C: Configuration<D>> configureSuspended(noinline sideEffect: suspend C.()->Unit) : D {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configureSuspended()
}

inline fun <D,  reified C: Configuration<D>, reified Data> configure(data: Data, noinline sideEffect:  C.()->Unit) : D {
    var n: C? = null
    C::class.constructors.forEach {
        try {
            n = it.call(data)
        }
        catch (exception: Exception){/* unimportant */}
    }
    if(n == null) {
        throw Exception("Constructor does not take arguments of type ${Data::class}")
    }
    n!!.sideEffect()
    return n!!.configure()
}

inline fun <D,  reified C: Configuration<D>> configure(data: Array<out Any?>, noinline sideEffect:  C.()->Unit) : D {
    var n: C? = null
    C::class.constructors.forEach {
        try {
            n = it.call(*data)
        }
        catch (exception: Exception){/* unimportant */}
    }
    if(n == null) {
        throw Exception("Constructor does not take arguments of these types")
    }
    n!!.sideEffect()
    return n!!.configure()
}

fun constructor(vararg args : Any?): Array<out Any?> = args

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
/*
suspend inline fun <D, reified C: SuspendedConfiguration<D>> configureSuspended(noinline  sideEffect: suspend C.()->Unit) : Evolving<D> {
    val c = C::class.createInstance()
    c.sideEffect()
    return c.configure()
}
 */