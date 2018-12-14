package org.drx.evoleq.examples.application.fx

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.Stage
import org.drx.evoleq.dsl.Configurations
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.dsl.block
import org.drx.evoleq.dsl.termBlock
import org.drx.evoleq.examples.application.dsl.ConfigurationEntry
import kotlin.reflect.KClass

class FxApp<D>  : tornadofx.App() {

    private val configurations: Configurations by lazy{ CONFIGURATIONS }
    private val stubProperty: SimpleObjectProperty<ApplicationStub<*>> by lazy { STUB_PROPERTY }

    val stub: ApplicationStub<D> = configurations.get<StubBlock>().configure() as ApplicationStub<D>
    private val initBlock: ()->Unit = configurations.get<InitBlock>().configure() as ()->Unit
    // Fx stuff
    private val fxInitBlock: ()->Unit = configurations.get<FxInitBlock>().configure() as ()->Unit
    private val fxStartBlock: Stage.()->Unit = configurations.get<FxStartBlock>().configure() as Stage.()->Unit
    private val fxStopBlock: ()->Unit = configurations.get<FxStopBlock>().configure() as ()->Unit

    init{
        // register configuration functions
        CONFIGURATIONS.registry[RegisterConfigBlock::class] = termBlock<ConfigurationEntry> {
            val f: (ConfigurationEntry) -> Unit = { entry -> CONFIGURATIONS.registry[entry.key] = entry.config }
            block = f
        }
        CONFIGURATIONS.registry[RegisterConfigsBlock::class] = termBlock<Array<ConfigurationEntry>> {
            val f: (Array<ConfigurationEntry>) -> Unit = { entries ->
                entries.forEach { entry -> CONFIGURATIONS.registry[entry.key] = entry.config }
            }
            block = f
        }
        // register fx-application functions
        CONFIGURATIONS.registry[FxStopFunction::class] = block { block = { stop() } }
        CONFIGURATIONS.registry[FxInitFunction::class] = block { block = { init() } }
        CONFIGURATIONS.registry[FxStartFunction::class] = termBlock<Stage> {
            val f: (Stage) -> Unit = { stage -> start(stage) }
            block = f
        }
        // register stage-manager functions
        CONFIGURATIONS.registry[ShowStageFunction::class] = termBlock<Stage> {
            val f: (Stage) -> Unit = {
                    stage -> show(stage)
            }
            block = f
        }
        CONFIGURATIONS.registry[CloseStageFunction::class] = termBlock<Stage> {
            val f: (Stage) -> Unit = { stage -> close(stage) }
            block = f
        }
        // apply init block
        initBlock()
        // set value of stub property
        stubProperty.value = stub
    }
    companion object Launcher {
        val CONFIGURATIONS: Configurations by lazy {Configurations()}
        val STUB_PROPERTY: SimpleObjectProperty<ApplicationStub<*>> by lazy { SimpleObjectProperty<ApplicationStub<*>>() }
    }

    /**
     * Stage Management
     * ================================================================================================================
     */
    private val stages: ArrayList<Stage> by lazy { ArrayList<Stage>() }
    fun show(stage: Stage) {
        Platform.runLater {
            stage.show()
            //stage.icons
        }
        stages.add(stage)
    }
    fun close(stage: Stage) {
        Platform.runLater {
            stage.close()
        }
        stages.remove(stage)
    }

    /**
     * Fx application stuff
     * ================================================================================================================
     */
    override fun init() {

        fxInitBlock()
    }

    override fun start(stage: Stage) {
        stage.fxStartBlock()
    }
    override fun stop() {
        fxStopBlock()
    }
}
