package org.drx.evoleq.examples.application.fx

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.spatulas
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.gap.Spatula
import org.drx.evoleq.math.consumeConfig
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

    override fun configure(): FxStage<D> {
        return object : FxStage<D>() {
            init{
                Platform.runLater {
                    // configure fx-stage as stage
                    val config = this@FxStageConfiguration

                    initStyle(config.style)
                    initModality(config.modality)
                    scene = config.scene!!

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
