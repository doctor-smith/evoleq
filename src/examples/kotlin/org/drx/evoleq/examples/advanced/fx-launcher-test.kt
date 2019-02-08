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
package org.drx.evoleq.examples.advanced

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
import org.drx.evoleq.examples.application.fx.FxApplicationLauncher


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

