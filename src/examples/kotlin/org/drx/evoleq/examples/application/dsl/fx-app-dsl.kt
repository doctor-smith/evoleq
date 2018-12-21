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
package org.drx.evoleq.examples.application.dsl

import javafx.stage.Stage
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.*
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.fx.*
import kotlin.reflect.KClass


open class FxAppConfiguration<D> : Configuration<ApplicationStub<D>> {
    /**
     * Setup configurations
     */
    private val configurations: Configurations by lazy { Configurations() }
    fun registerConfigurations(vararg configs: ConfigurationEntry) {
        configs.forEach { it -> configurations.registry[it.key]= it.config }
    }
    //val fxConfigurations: SuspendedConfigurations by lazy { SuspendedConfigurations() }
    val fxConfigurationEntries: ArrayList<SuspendedConfigurationEntry> = arrayListOf()
    fun registerFxConfigurations(vararg configs: SuspendedConfigurationEntry) {
        configs.forEach { it -> fxConfigurationEntries.add(it) }
    }
    /**
     * Stub / Launch
     * ================================================================================================================
     */
    var stubConfig = ApplicationStubConfiguration<D>()
    var initBlock: ()->Unit = {}
    /**
     * Fx stuff
     * ================================================================================================================
     */
    var fxInitBlock: ()->Unit = {}
    var fxStartBlock: Stage.()->Unit = {}
    var fxStopBlock: ()->Unit = {}
    /**
     * Configure app
     */
    override fun configure(): ApplicationStub<D> {
        FxApp.CONFIGURATIONS.registry.putAll(
            configurations.registry
        )
        FxApp.CONFIGURATIONS.registry[StubBlock::class] = stubConfig
        FxApp.CONFIGURATIONS.registry[InitBlock::class] = block { block = initBlock }
        FxApp.CONFIGURATIONS.registry[FxInitBlock::class] = block { block = fxInitBlock }
        FxApp.CONFIGURATIONS.registry[FxStartBlock::class] = block<Stage> { block = fxStartBlock }
        FxApp.CONFIGURATIONS.registry[FxStopBlock::class] = block { block = fxStopBlock }

        return stubConfig.configure()
    }
}

class ConfigurationEntry(val key: KClass<*>, val config: Configuration<*>)

class ConfigurationEntryConfiguration : Configuration<ConfigurationEntry> {
    var key: KClass<*>? = null
    var config: Configuration<*>? = null

    override fun configure(): ConfigurationEntry =
        ConfigurationEntry(key!!, config!!)
}

fun entry(configure: ConfigurationEntryConfiguration.()->Unit): ConfigurationEntry = configure(configure)

class SuspendedConfigurationEntry(val key: KClass<*>, val config: SuspendedConfiguration<*>)

class SuspendedConfigurationEntryConfiguration : Configuration<SuspendedConfigurationEntry> {
    var key: KClass<*>? = null
    var config: SuspendedConfiguration<*>? = null

    override fun configure(): SuspendedConfigurationEntry =
        SuspendedConfigurationEntry(key!!, config!!)
}

fun fxEntry(configure: SuspendedConfigurationEntryConfiguration.()->Unit): SuspendedConfigurationEntry = configure(configure)


//suspend fun entryS(configure: suspend ConfigurationEntryConfiguration.()->Unit): ConfigurationEntry = configure(configure)

fun <D> fxApp(configure: FxAppConfiguration<D>.()->Unit) = configure(configure) //: FxApp<D>

fun <D> fxAppConfiguration(config: FxAppConfiguration<D>.()->Unit): FxAppConfiguration<D> {
    val configuration = FxAppConfiguration<D>()
    configuration.config()
    return configuration
}