package org.drx.evoleq.experimental.app.fx

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.stage.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.Configurations
import org.drx.evoleq.evolve
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Parallel


class App : tornadofx.App() /*,AppStub*/ {

    val configurations: Configurations by lazy { Configurations() }
    /**
     * Stub / Launch
     * ================================================================================================================
     */
    private var stub = object : AppStub {
        override fun doIt(m: String): Evolving<String> = Parallel{
            delay(1000)
            m
        }
    }
    init{
        stubProperty.value = stub
    }
    companion object Launcher {
        val stubProperty: SimpleObjectProperty<AppStub> by lazy { SimpleObjectProperty<AppStub>() }
    }


    /**
     * Fx stuff
     * ================================================================================================================
     */
    override fun init() {

    }

    override fun start(stage: Stage) {
        stage.scene = Scene(Pane(),200.0,200.0)
        stage.title = "Title"
        val button = Button("Click me")
        stage.show()
    }
}



interface AppStub {
    fun doIt(m: String): Evolving<String>


}



interface View<T> {
    fun build(): T
}

fun main(args: Array<String>) { runBlocking {
    val a = FxApplicationLauncher(App.stubProperty)
        .launch<App>()
    val stub = a.get()
    evolve(
        "",
        once()

    ) {
        s: String -> stub.doIt(s)
    }
    System.exit(0)
    }
}

