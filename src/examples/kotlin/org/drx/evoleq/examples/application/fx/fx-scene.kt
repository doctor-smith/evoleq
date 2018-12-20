package org.drx.evoleq.examples.application.fx

import javafx.scene.Group
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.SuspendedConfiguration
import org.drx.evoleq.dsl.spatulas
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.app_filesystem.fx.ParallelFx
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.gap.Spatula
import org.drx.evoleq.time.Keeper
import org.drx.evoleq.time.waitForValueToBeSet
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

abstract class FxScene<D>(parent: Parent) : Scene(parent), FxComponent<D> {

}

open class FxSceneConfiguration<D> : Configuration<FxScene<D>> {

    var parent: Parent? = null
    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }

    override fun configure(): FxScene<D> {

        while(parent == null) {
            Thread.sleep(1)
        }
        return object : FxScene<D>(parent!!) {
            val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
            override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

            override suspend fun stub(d: D): Evolving<D> {
                return this@FxSceneConfiguration.stub(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D> {
                    filler = this@FxSceneConfiguration.stub
                }.spatula(w)
            }
        }
    }
}

inline fun <D,reified S: FxSceneConfiguration<D>> fxSceneConfiguration(conf: S.()->Unit): S {
    val s = S::class.createInstance()
    s.conf()
    return s
}

// custom configuration

inline fun <D,reified S: FxSceneConfiguration<D>> customConfiguration(conf: S.()->Unit = {}): S {
    val s = S::class.createInstance()
    s.conf()
    return s
}



open class FxSceneLazyConfiguration<D> : SuspendedConfiguration<FxScene<D>> {

    var parent: Parent? = null
    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }

    override suspend fun configure(): Evolving<FxScene<D>> {

        while(parent == null) {
            delay(1)
        }
        return ParallelFx{object : FxScene<D>(parent!!) {
            val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
            override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

            override suspend fun stub(d: D): Evolving<D> {
                return this@FxSceneLazyConfiguration.stub(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D> {
                    filler = this@FxSceneLazyConfiguration.stub
                }.spatula(w)
            }
        }}
    }
}

inline fun <D,reified S: FxSceneLazyConfiguration<D>> fxSceneLazyConfiguration(conf: S.()->Unit): S {
    val s = S::class.createInstance()
    s.conf()
    return s
}
fun <D> FxSceneLazyConfiguration<D>.fxStub(config:  FxSceneLazyConfiguration<D>.()->Unit): FxSceneLazyConfiguration<D>  {
    //return fx{
    this.config()
    return this
    //}
}
