package org.drx.evoleq.examples.application.fx

import javafx.scene.Node
import javafx.scene.control.Button
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.spatulas
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.gap.Spatula
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class FxNode<D,N: Node>(val node: N) : FxComponent<D>

open class FxNodeConfiguration<D,N:Node> : Configuration<FxNode<D,N>> {
    var node : N? = null

    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }
    override fun configure(): FxNode<D,N> = object: FxNode<D,N>(node!!) {

        override suspend fun stub(d: D): Evolving<D> {
            return this@FxNodeConfiguration.stub(d)
        }

        override suspend fun <W> spatula(w: W): Spatula<W, D> {
            return spatulas<D>{
                filler = this@FxNodeConfiguration.stub
            }.spatula(w)
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