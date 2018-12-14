package org.drx.evoleq.examples.app_preloader.preloader

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.input.KeyCombination
import javafx.scene.layout.Pane
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import org.drx.evoleq.dsl.*
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.application.fx.FxComponent
import org.drx.evoleq.gap.Spatula

class PreloaderKey
abstract class PreLoader<D> : Stage(), FxComponent<D>
class PreLoaderConfiguration<D> : Configuration<PreLoader<D>> {

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
    var stub: (D)-> Evolving<D> = {d -> Immediate{d}}
    // custom content
    var label: Label? = null

    override fun configure(): PreLoader<D> {
        return object : PreLoader<D>() {



            init{
                Platform.runLater {
                    // configure preLoader as stage
                    val config = this@PreLoaderConfiguration

                    initStyle(config.style)
                    initModality(config.modality)

                    // scene
                    scene = config.scene!!

                    // icons
                    icons.addAll(config.icons)

                    // title
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


                    label = Label("0")
                    (scene.root as Pane).children.add(label!!)
                }

            }

            override suspend fun stub(d: D): Evolving<D> {
                return this@PreLoaderConfiguration.stub(d)
            }

            override suspend fun <W> spatula(w: W): Spatula<W, D> {
                return spatulas<D>{
                    filler = this@PreLoaderConfiguration.stub
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

fun <D> preLoader(config: PreLoaderConfiguration<D>.()->Unit): PreLoaderConfiguration<D> {
    val p = PreLoaderConfiguration<D>()
    p.config()
    return p
}

