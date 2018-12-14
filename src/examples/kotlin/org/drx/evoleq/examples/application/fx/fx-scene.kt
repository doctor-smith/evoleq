package org.drx.evoleq.examples.application.fx

import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.Region
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.spatulas
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.gap.Spatula
import kotlin.reflect.full.createInstance

abstract class FxScene<D>(parent: Parent) : Scene(parent), FxComponent<D>

open class FxSceneConfiguration<D> : Configuration<FxScene<D>> {

    var parent: Parent? = null
    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }

    override fun configure(): FxScene<D> = object: FxScene<D>(parent!!) {


        override suspend fun stub(d: D): Evolving<D> {
            return this@FxSceneConfiguration.stub(d)
        }

        override suspend fun <W> spatula(w: W): Spatula<W, D> {
            return spatulas<D>{
                filler = this@FxSceneConfiguration.stub
            }.spatula(w)
        }
    }
}

// custom configuration

inline fun <D,reified S: FxSceneConfiguration<D>> customConfiguration(conf: S.()->Unit = {}): S {
    val s = S::class.createInstance()
    s.conf()
    return s
}