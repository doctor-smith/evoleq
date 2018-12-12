package org.drx.evoleq.experimental.dsl

import javafx.stage.Stage
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.Configurations
import org.drx.evoleq.dsl.configure
import org.drx.evoleq.experimental.app.fx.*
import kotlin.reflect.KClass


open class FxAppConfiguration<D> : Configuration<Unit> {
    /**
     * Setup configurations
     */
    private val configurations: Configurations by lazy { Configurations() }
    fun registerConfigurations(vararg items: ConfigurationEntry) {
        items.forEach { it ->
            configurations.registry[it.key]= it.config
        }
    }
    /**
     * Stub / Launch
     * ================================================================================================================
     */
    var stubConfig = ApplicationStubConfiguration<D> ()
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
    override fun configure() {


        FxApp.CONFIGURATIONS.registry.putAll(
            configurations.registry
        )
        FxApp.CONFIGURATIONS.registry[StubBlock::class] = stubConfig
        FxApp.CONFIGURATIONS.registry[InitBlock::class] = block{ block = initBlock }
        FxApp.CONFIGURATIONS.registry[FxInitBlock::class] = block{ block = fxInitBlock }
        FxApp.CONFIGURATIONS.registry[FxStartBlock::class] = block<Stage>{ block = fxStartBlock }
        FxApp.CONFIGURATIONS.registry[FxStopBlock::class] = block{ block = fxStopBlock }

    }



}

class ConfigurationEntry(val key: KClass<*>, val config: Configuration<*>)
class ConfigurationEntryConfiguration() : Configuration<ConfigurationEntry> {
    var key: KClass<*>? = null
    var config: Configuration<*>? = null

    override fun configure(): ConfigurationEntry = ConfigurationEntry(key!!,config!!)
}
fun entry(configure: ConfigurationEntryConfiguration.()->Unit): ConfigurationEntry = configure(configure)

fun <D> fxApp(configure: FxAppConfiguration<D>.()->Unit) = configure(configure) //: FxApp<D>