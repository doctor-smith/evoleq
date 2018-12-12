package org.drx.evoleq.experimental.app.fx

import javafx.beans.property.SimpleObjectProperty
import javafx.stage.Stage
import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.dsl.Configurations
import org.drx.evoleq.dsl.configure
import org.drx.evoleq.dsl.setupConfiguration
import org.drx.evoleq.experimental.app.ApplicationStub

class FxApp<D>  : tornadofx.App() {

    private val configurations: Configurations by lazy{ CONFIGURATIONS }
    private val stubProperty: SimpleObjectProperty<ApplicationStub<*>> by lazy { STUB_PROPERTY}

    val stub: ApplicationStub<D> = configurations.get<StubBlock>().configure() as ApplicationStub<D>
    private val initBlock: ()->Unit = configurations.get<InitBlock>().configure() as ()->Unit
    // Fx stuff
    private val fxInitBlock: ()->Unit = configurations.get<FxInitBlock>().configure() as ()->Unit
    private val fxStartBlock: Stage.()->Unit = configurations.get<FxStartBlock>().configure() as Stage.()->Unit
    private val fxStopBlock: ()->Unit = configurations.get<FxStopBlock>().configure() as ()->Unit

    init{
        CONFIGURATIONS.registry[FxStopFunction::class] = block{ block = {stop()} }
        CONFIGURATIONS.registry[FxInitFunction::class] = block{ block = {init()} }
        //CONFIGURATIONS.registry[FxStartFunction::class] = block{ block = {start()} }

        initBlock()
        stubProperty.value = stub
    }
    companion object Launcher {
        val CONFIGURATIONS: Configurations by lazy {Configurations()}

        val STUB_PROPERTY: SimpleObjectProperty<ApplicationStub<*>> by lazy { SimpleObjectProperty<ApplicationStub<*>>() }
    }

    /**
     * Fx stuff
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

sealed class FxBlock
object FxInitBlock : FxBlock()
object FxStartBlock : FxBlock()
object FxStopBlock : FxBlock()
object FxInitFunction : FxBlock()
object FxStartFunction : FxBlock()
object FxStopFunction : FxBlock()

class BlockConfiguration : Configuration<()->Unit> {
    var block: ()->Unit = {}
    override fun configure(): () -> Unit {
        return block
    }
}

sealed class AppBlock
object StubBlock : AppBlock()
object InitBlock : AppBlock()

fun block(config: BlockConfiguration.()->Unit): BlockConfiguration {
    val b = BlockConfiguration()
    b.config()
    return b
}
class ExtensionBlockConfiguration<T> : Configuration<T.()->Unit> {
    var block: T.()->Unit = {}
    override fun configure(): T.() -> Unit {
        return block
    }
}
fun <T> block(config:ExtensionBlockConfiguration<T>.()->Unit): ExtensionBlockConfiguration<T>{
    val b = ExtensionBlockConfiguration<T>()
    b.config()
    return b
}