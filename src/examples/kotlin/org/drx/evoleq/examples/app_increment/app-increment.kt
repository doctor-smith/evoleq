package org.drx.evoleq.examples.app_increment

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.scene.paint.Stop
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.*
import org.drx.evoleq.EvolutionConditions
import org.drx.evoleq.Evolving
import org.drx.evoleq.Parallel
import org.drx.evoleq.evolve
import tornadofx.ChangeListener

import tornadofx.action

data class Data(
    val app: IApp<Data>,
    val message: Message,
    val cnt: Int
)

interface IApp<D> {
    fun startApp(data: D): Evolving<D>
    fun stopApp(data: D): Evolving<D>
    fun updateApp(data: D): Evolving<D>
    fun waiting(data: D): Evolving<D>
}

sealed class Message {
    object StartApp : Message()
    object StartUp : Message()
    object LaunchingApp : Message()
    object InitializingApp : Message()
    object StartedApp : Message()
    object ClickedIncButton : Message()
    object StopApp : Message()
    object UpdatedApp : Message()
    object StoppedApp : Message()
    object Wait : Message()
    object Empty : Message()
}

class App : tornadofx.App(), IApp<Data> {
    private object Holder { val INSTANCE = App() }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
    }
    private val out = SimpleObjectProperty<Data>()
    private val input = SimpleObjectProperty<Data>()

    override fun init() {
        instance.out.value = instance.input.value.copy(message = Message.InitializingApp)
    }
    override fun start(stage: Stage) {
        stage.initStyle(StageStyle.UNDECORATED)
        val visualBounds = Screen.getPrimary().visualBounds
        val width = 200.0
        val height = 200.0
        val scene = Scene(Pane(),width,height)
        stage.scene = scene
        stage.title = "Example Application"
        val button = Button("CLick me!")

        button.action{
            instance.out.value = instance.input.value.copy(message = Message.ClickedIncButton)
        }

        button.resize(200.0, 30.0)
        button.layoutX = 0.0
        val label = Label("0")
        label.layoutX = 100.0
        label.resize(100.0,30.0)
        instance.input.addListener{_,_,nv -> label.text = "${nv.cnt}"}
        val stop = Button("Stop")
        stop.resize(100.0, 30.0)
        stop.layoutX = 150.0
        stop.layoutY = 170.0
        stop.action {
            instance.out.value = instance.input.value.copy(message = Message.StopApp)
        }
        (scene.root as Pane).children.addAll(
            button,
            label,
            stop
        )
        instance.out.value = instance.input.value.copy(message = Message.StartedApp)
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
        Data(this@App,Message.LaunchingApp,data.cnt)
    }

    override fun updateApp(data: Data): Evolving<Data> = Parallel {
        Platform.runLater {
            instance.input.value = data
        }
        Data(this@App,Message.UpdatedApp,data.cnt)
    }

    override fun stopApp(data: Data): Evolving<Data> = Parallel {
        Platform.runLater {
            stop()
        }
        Data(this@App,Message.StoppedApp,data.cnt)
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
            initialData = Data(App.instance, Message.StartApp as Message, 0),
            conditions = EvolutionConditions(
                testObject = Pair(Message.StartUp as Message, 0),
                check ={ when(it.first){
                    is Message.StopApp -> false
                    else ->true
                }  && it.second < 100 },
                updateCondition = { data -> Pair(data.message, data.cnt) }
            )
        ){ data -> println(data.message)
            when (data.message) {
                is Message.StartApp -> data.app.startApp(data)
                is Message.LaunchingApp ->data.app.waiting(Data(data.app, Message.Wait, data.cnt))
                is Message.InitializingApp -> data.app.waiting(Data(data.app, Message.Wait, data.cnt))
                is Message.StartedApp -> data.app.updateApp(Data(data.app, Message.Empty, data.cnt))
                is Message.ClickedIncButton -> data.app.updateApp(Data(data.app, Message.Empty, data.cnt + 1))
                is Message.StopApp -> data.app.stopApp(Data(data.app, Message.Empty, data.cnt))
                else -> data.app.waiting(Data(data.app, Message.Wait, data.cnt))
            }
        }
    }
}



