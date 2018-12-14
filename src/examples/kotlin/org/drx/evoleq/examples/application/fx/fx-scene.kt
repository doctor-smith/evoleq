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

    //var width: Double = 0.0

    var content: Node? = null


    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }


    // custom configuration

    inline fun <reified S: FxSceneConfiguration<D>> customConfiguration(conf: S.()->Unit = {}): S {
        val s = S::class.createInstance()
        s.conf()
        return s
    }

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

