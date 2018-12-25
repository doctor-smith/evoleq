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
package org.drx.evoleq.examples.application.fx

import javafx.application.Platform
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.SuspendedConfiguration
import org.drx.evoleq.dsl.spatulas
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.app_filesystem.fx.ParallelFx
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.gap.Spatula
import org.drx.evoleq.math.consumeConfig
import org.drx.evoleq.time.Keeper
import org.drx.evoleq.time.waitForValueToBeSet
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance


abstract class FxStage<D> : Stage(), FxComponent<D>
open class FxStageConfiguration<D> : Configuration<FxStage<D>> {

    var scene : Scene? = null
    var modality: Modality = Modality.NONE
    var style: StageStyle = StageStyle.DECORATED
    var title : String = ""
    var icons : ArrayList<Image> = arrayListOf()

    // stage geometry

    var x: Double = 0.0
    var y: Double = 0.0

    var maxWidth: Double = 0.0
    var maxHeight: Double = 0.0
    var minWidth: Double = 0.0
    var minHeight: Double = 0.0

    // stage properties
    var isAlwaysOnTop: Boolean = false
    var isFullScreen: Boolean = false
    var isIconified: Boolean = false
    var isMaximized: Boolean = false
    var isResizable: Boolean = false

    var fullScreenExitHint = "Press 'CTRL+E' to exit full screen mode"
    var fullScreenExitKeyCombination = KeyCombination.keyCombination("CTRL+E")!!

    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }
    val stubs = HashMap<KClass<*>, Stub<*>>()

    override fun configure(): FxStage<D> {
        return object : FxStage<D>() {
            init{
                Platform.runLater {
                    // configure fx-stage as stage
                    val config = this@FxStageConfiguration

                    initStyle(config.style)
                    initModality(config.modality)
                    stubs().putAll(config.stubs)
                    var s:Scene? = null
                    GlobalScope.launch{ GlobalScope.async {
                        //Platform.runLater {
                            s = Keeper(config.scene).waitForValueToBeSet().get()
                            //scene = config.scene!!
                        //}
                    }.await()
                        Platform.runLater {
                            scene = s!!
                        }
                    }


                    icons.addAll(config.icons)

                    title = config.title


                    // geometry
                    x = config.x
                    y = config.y
                    maxHeight = config.maxHeight
                    maxWidth = config.maxWidth
                    minHeight = config.minHeight
                    minWidth = config.minWidth

                    isAlwaysOnTop = config.isAlwaysOnTop
                    isFullScreen = config.isFullScreen
                    //isIconified = stage.isIconified
                    isMaximized = config.isMaximized
                    isResizable = config.isResizable

                    fullScreenExitKeyCombination = config.fullScreenExitKeyCombination
                    fullScreenExitHint = config.fullScreenExitHint

                    }

            }

            private val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
            override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

            override suspend fun stub(d: D): Evolving<D> {
                return this@FxStageConfiguration.stub(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D>{
                    filler = this@FxStageConfiguration.stub
                }.spatula(w)
            }
        }
    }
}



fun stageConfiguration(config: Stage.()->Unit): Stage {
    val s = Stage()
    s.config()
    return s
}

inline fun <D,reified S>
fxStageConfiguration(
    noinline config:  S.()->Unit
): S where S:FxStageConfiguration<D> {
    val p = S::class.createInstance()
    p.config()
    return p
}

inline fun <D, reified S0, reified  S: S0>
fxStageConfiguration(
    stageConfig: S,
    noinline config: S0.() -> Unit
): S where S0:FxStageConfiguration<D> =
    consumeConfig<S0,S>(stageConfig,config)
/*
inline fun <D, reified S0:FxStageConfiguration<D>, reified  S: FxStage<D>>
fxStageConsumeConfig(noinline config: S0.() -> Unit) : Configuration<S>  {

    val s0 = fxStageConfiguration(config)


    val sC = object : Configuration<S> {
        override fun configure(): S {

        }
    }
}
        */

/*
inline fun <D, reified S: FxStage<D>> fxStageConfiguration(noinline config: S.() -> S): Configuration<S> {
    val c = FxStageConfiguration<D>()

    val c0 = object: Configuration<FxStage<D>> {
        override fun configure(): FxStage<D> = c.configure()
    }

    val c1 = object: Configuration<S> {
        override fun configure(): S {
            val x = c0.configure() as S
            return x.config()
        }

    }
    return c1
}
*/



open class FxStageLazyConfiguration<D> : SuspendedConfiguration<FxStage<D>> {

    var scene : Scene? = null
    var modality: Modality = Modality.NONE
    var style: StageStyle = StageStyle.DECORATED
    var title : String = ""
    var icons : ArrayList<Image> = arrayListOf()

    // stage geometry

    var x: Double = 0.0
    var y: Double = 0.0

    var maxWidth: Double = 0.0
    var maxHeight: Double = 0.0
    var minWidth: Double = 0.0
    var minHeight: Double = 0.0

    // stage properties
    var isAlwaysOnTop: Boolean = false
    var isFullScreen: Boolean = false
    var isIconified: Boolean = false
    var isMaximized: Boolean = false
    var isResizable: Boolean = false

    var fullScreenExitHint = "Press 'CTRL+E' to exit full screen mode"
    var fullScreenExitKeyCombination = KeyCombination.keyCombination("CTRL+E")!!

    // stub
    var stub: (D)-> Evolving<D> = { d -> Immediate{d} }
    val stubs = HashMap<KClass<*>, Stub<*>>()

    override suspend fun configure(): Evolving<FxStage<D>> {
        return ParallelFx{ object : FxStage<D>() {
            init{
                Platform.runLater {
                    // configure fx-stage as stage
                    val config = this@FxStageLazyConfiguration

                    initStyle(config.style)
                    initModality(config.modality)

                    var s:Scene? = null
                    GlobalScope.launch{ async {
                        //Platform.runLater {
                        s = Keeper(config.scene).waitForValueToBeSet().get()
                        //scene = config.scene!!
                        //}
                    }.await()
                        Platform.runLater {
                            scene = s!!

                        }
                    }
                    stubs().putAll(config.stubs)

                    icons.addAll(config.icons)

                    title = config.title


                    // geometry
                    x = config.x
                    y = config.y
                    maxHeight = config.maxHeight
                    maxWidth = config.maxWidth
                    minHeight = config.minHeight
                    minWidth = config.minWidth

                    isAlwaysOnTop = config.isAlwaysOnTop
                    isFullScreen = config.isFullScreen
                    //isIconified = stage.isIconified
                    isMaximized = config.isMaximized
                    isResizable = config.isResizable

                    fullScreenExitKeyCombination = config.fullScreenExitKeyCombination
                    fullScreenExitHint = config.fullScreenExitHint

                }

            }

            private val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
            override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

            override suspend fun stub(d: D): Evolving<D> {
                return this@FxStageLazyConfiguration.stub(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D>{
                    filler = this@FxStageLazyConfiguration.stub
                }.spatula(w)
            }
        }}
    }
}





inline fun <D,reified S>
        fxStageLazyConfiguration(
    noinline config:  S.()->Unit
): S where S:FxStageLazyConfiguration<D> {
    val p = S::class.createInstance()
    p.config()
    return p
}

fun <D> FxStageLazyConfiguration<D>.fxStub(config:  FxStageLazyConfiguration<D>.()->Unit): FxStageLazyConfiguration<D>  {
    //return fx{
    this.config()
    return this
    //}
}
