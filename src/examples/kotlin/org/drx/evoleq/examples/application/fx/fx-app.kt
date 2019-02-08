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
package org.drx.evoleq.examples.application.fx

import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.stage.Stage
import org.drx.evoleq.dsl.*
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.dsl.ConfigurationEntry
import kotlin.reflect.KClass

class FxApp<D>  : tornadofx.App() {

    private val configurations: Configurations by lazy{ CONFIGURATIONS }
    private val stubProperty: SimpleObjectProperty<ApplicationStub<*>> by lazy { STUB_PROPERTY }
    //private toolkitInitializedProoperty: SimpleBooleanProperty by lazy {}
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
        CONFIGURATIONS.registry[RegisterStageFunction::class] = termBlock<KClass<*>,Stage>{
            val f: (KClass<*>,Stage) -> Unit = {key, stage -> register(key,stage) }
            block = f
        }
        CONFIGURATIONS.registry[UnregisterStageFunction::class] = functionBlock<KClass<*>,Stage>{
            val f: (KClass<*>) -> Stage = { key -> unregister(key) }
            block = f
        }

        // apply init block
        initBlock()

    }
    companion object Launcher {
        val CONFIGURATIONS: Configurations by lazy {Configurations()}
        val FX_CONFIGURATIONS: SuspendedConfigurations by lazy {SuspendedConfigurations()}
        val STUB_PROPERTY: SimpleObjectProperty<ApplicationStub<*>> by lazy { SimpleObjectProperty<ApplicationStub<*>>() }
        val TOOLKIT_INIT_PROPERTY: SimpleObjectProperty<Boolean> by lazy { SimpleObjectProperty<Boolean>() }
    }

    /**
     * Stage Management
     * ================================================================================================================
     */
    private val stages: HashMap<KClass<*>,Stage> by lazy { HashMap<KClass<*>,Stage>() }
    fun register(key: KClass<*>,stage:  Stage) { stages[key] = stage }
    fun unregister(key: KClass<*>): Stage{
        val stage = stages[key]!!
        stages.remove(key)
        return stage
    }
    fun show(stage: Stage) {
        Platform.runLater {
            stage.show()
            //stage.icons
        }

    }
    fun close(stage: Stage) {
        Platform.runLater {
            stage.close()
        }

    }

    /**
     * Fx application stuff
     * ================================================================================================================
     */
    override fun init() {

        fxInitBlock()
    }

    override fun start(stage: Stage) {
        TOOLKIT_INIT_PROPERTY.value = true
        // set value of stub property
        stubProperty.value = stub
        stage.fxStartBlock()
    }
    override fun stop() {
        fxStopBlock()
    }
}
