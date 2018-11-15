package org.drx.evoleq.examples.app_increment

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.*
import org.drx.evoleq.EvolutionConditions
import org.drx.evoleq.Evolving
import org.drx.evoleq.Parallel
import org.drx.evoleq.evolve
import tornadofx.ChangeListener

import tornadofx.action

data class Data(
    val app: IApp<Data>,
    val message: String,
    val cnt: Int
)

/**
 * TODO AppIO Monad
 */

class App : tornadofx.App(), IApp<Data> {
    private object Holder { val INSTANCE = App() }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
    }
    private val out = SimpleObjectProperty<Data>()
    private val input = SimpleObjectProperty<Data>()

    override fun init() {
        instance.out.value = instance.input.value.copy(message = "initializing")
    }
    override fun start(stage: Stage) {
        val visualBounds = Screen.getPrimary().visualBounds
        val width = visualBounds.width-400
        val height = visualBounds.height-400
        val scene = Scene(FlowPane(),width,height)
        stage.scene = scene
        stage.title = "Example Application"
        val button = Button("CLick me!")
        button.action{
            instance.out.value = instance.input.value.copy(message = "clicked")
        }
        val label = Label("0")
        instance.input.addListener{_,_,nv -> label.text = "${nv.cnt}"}
        val stop = Button("Stop")
        stop.action {
            instance.out.value = instance.input.value.copy(message = "stop")
        }
        (scene.root as FlowPane).children.addAll(
            button,
            label,
            stop
        )
        instance.out.value = instance.input.value.copy(message = "started")
        stage.show()
    }

    override fun stop() {
        System.exit(0)
    }
    override fun startApp(data: Data): Evolving<Data> = Parallel {
        GlobalScope.launch {
            input.value = data
            val x = Application.launch(App::class.java)
        }
        Data(this@App,"launching-app",data.cnt)
    }

    override fun updateApp(data: Data): Evolving<Data> = Parallel {
        Platform.runLater {
            instance.input.value = data
        }
        Data(this@App,"updated",data.cnt)
    }

    override fun stopApp(data: Data): Evolving<Data> = Parallel {
        Platform.runLater {
            stop()
        }
        Data(this@App,"stopped",data.cnt)
    }

    override fun waiting(data: Data): Evolving<Data> = Parallel {
        changes().get()
    }

    private fun  changes(): Evolving<Data> = Parallel {
        var m:Data
        var changed = false
        val listener = ChangeListener<Data> {_,_,nv -> m = nv; changed = true }
        instance.out.addListener(listener)
        while(!changed){
            Thread.sleep(10)
        }
        instance.out.removeListener(listener)
        instance.out.value
    }
}

fun main(args: Array<String>) {
    runBlocking {
        evolve(
            initialData = Data(App.instance, "start-app", 0),
            conditions = EvolutionConditions(
                testObject = Pair("startup", 0),
                check ={ it.first != "stopped" && it.second < 100 },
                updateCondition = { data -> Pair(data.message, data.cnt) }
            )
        ){ data -> println(data.message)
            when (data.message) {
                "start-app" -> data.app.startApp(data)
                "launching-app" ->data.app.waiting(Data(data.app, "", data.cnt))
                "initializing" -> data.app.waiting(Data(data.app, "", data.cnt))
                "started" -> data.app.updateApp(Data(data.app, "", data.cnt))
                "clicked" -> data.app.updateApp(Data(data.app, "", data.cnt + 1))
                "stop" -> data.app.stopApp(Data(data.app, "", data.cnt))
                else -> data.app.waiting(Data(data.app, "", data.cnt))
            }
        }
    }
}



interface IApp<D> {
    fun startApp(data: D): Evolving<D>
    fun stopApp(data: D): Evolving<D>
    fun updateApp(data: D): Evolving<D>
    fun waiting(data: D): Evolving<D>
}

