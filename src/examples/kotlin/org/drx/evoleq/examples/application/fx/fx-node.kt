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
package org.drx.evoleq.examples.application.fx

import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.control.Button
import kotlinx.coroutines.delay
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.SuspendedConfiguration
import org.drx.evoleq.dsl.spatulas
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.app_filesystem.fx.ParallelFx
import org.drx.evoleq.examples.app_filesystem.fx.fx
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.gap.Spatula
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class FxNode<D,N: Node>(val node: N) : FxComponent<D>{
    //constructor(fx: suspend ()->N): this(fx())
}

open class FxNodeConfiguration<D,N:Node> : Configuration<FxNode<D,N>> {
    var node : N? = null

    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }
    override fun configure(): FxNode<D,N> {
/*
        while(node == null) {
            Thread.sleep(1)
        }
*/
        return object : FxNode<D, N>(node!!) {

            val stubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }
            override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

            override suspend fun stub(d: D): Evolving<D> {
                return this@FxNodeConfiguration.stub(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D> {
                    filler = this@FxNodeConfiguration.stub
                }.spatula(w)
            }
        }
    }
}

inline fun <D, N:Node, reified C: FxNodeConfiguration<D,N>>
        fxGenericNodeConfiguration(
    noinline config: C.()->Unit
) : C  {
    val fxNodeConfig = C::class.createInstance()
    fxNodeConfig.config()
    return fxNodeConfig
}
inline fun <D, N:Node> fxNodeConfiguration(
    noinline config: FxNodeConfiguration<D,N>.()->Unit
) : FxNodeConfiguration<D,N>  {
    val fxNodeConfig = FxNodeConfiguration<D,N>()
    fxNodeConfig.config()
    return fxNodeConfig
}

fun <D,N:Node> fxNodeConfiguration(n :N) : FxNodeConfiguration<D,N> {
    return object : FxNodeConfiguration<D,N>() {
        init{
            node = n
        }
    }
}

inline fun <N:Node> nodeConf(n:N, noinline config: N.()->Unit): N {
    n.config()
    return n
}

open class LazyFxNodeConfiguration<D,N:Node> : SuspendedConfiguration<FxNode<D, N>> {
    var node: (()->N)? = null
    //val accessibles = HashMap<KClass<*>,Node>()
    var stub: ()->((D)-> Evolving<D>) = {{ d -> Immediate{d} }}
    override suspend fun configure(): Evolving<FxNode<D,N>> {

        while(node == null) {
            delay(1)
        }

        return ParallelFx{ object : FxNode<D, N>(node!!()) {

            val stubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }
            override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

            override suspend fun stub(d: D): Evolving<D> {
                return this@LazyFxNodeConfiguration.stub()(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D> {
                    filler = this@LazyFxNodeConfiguration.stub()
                }.spatula(w)
            }
        }}
    }
}

fun <D,N: Node> fxNodeLazyConfiguration(block:()->N) : LazyFxNodeConfiguration<D,N> {
    return object : LazyFxNodeConfiguration<D,N>() {
        init{
            node = block
        }
    }
}
fun <D,N: Node> LazyFxNodeConfiguration<D,N>.fxStub(config:  LazyFxNodeConfiguration<D,N>.()->Unit): LazyFxNodeConfiguration<D,N>  {
    //return fx{
        this.config()
        return this
    //}
}
fun <T> pick(t: T) = {t}
val x = fxNodeLazyConfiguration<Int,Group>(pick(Group()))
val y = x.fxStub{
    stub = pick({s:Int -> Immediate{s+1}})
}