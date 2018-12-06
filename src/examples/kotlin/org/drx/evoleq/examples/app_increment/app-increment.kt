package org.drx.evoleq.examples.app_increment

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.*
import org.drx.evoleq.conditions.EvolutionConditions
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

class IO<D> {
    val output = SimpleObjectProperty<D>()
    val input = SimpleObjectProperty<D>()
}

class App : tornadofx.App(), IApp<Data> {
    private object Holder { val INSTANCE = App() }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
    }
    private val io = IO<Data>()

    override fun init() {
        instance.io.output.value = instance.io.input.value.copy(message = Message.InitializingApp)
    }
    override fun start(stage: Stage) {
        stage.initStyle(StageStyle.UNDECORATED)
        //val visualBounds = Screen.getPrimary().visualBounds
        val width = 200.0
        val height = 200.0
        val scene = Scene(AppContent(instance.io),width,height)
        stage.scene = scene
        stage.title = "Example Application"

        instance.io.output.value = instance.io.input.value.copy(message = Message.StartedApp)
        stage.show()
    }

    override fun stop() {
        Platform.exit()
    }
    override fun startApp(data: Data): Evolving<Data> = Parallel {
        GlobalScope.launch {
            io.input.value = data
            val x = launch(App::class.java)
        }
        Data(this@App, Message.LaunchingApp, data.cnt)
    }

    override fun updateApp(data: Data): Evolving<Data> = Parallel {
        Platform.runLater {
            instance.io.input.value = data
        }
        Data(this@App, Message.UpdatedApp, data.cnt)
    }

    override fun stopApp(data: Data): Evolving<Data> = Parallel {
        var running = true
        Platform.runLater {
            stop()
            running = false
        }
        while (running) {
            delay(10)
        }
        Data(this@App, Message.StoppedApp, data.cnt)
    }

    override fun waiting(data: Data): Evolving<Data> = Parallel {
        changes().get()
    }

    private fun  changes(): Evolving<Data> = Parallel {
        var m: Data
        var changed = false
        val listener = ChangeListener<Data> { _, _, nv -> m = nv; changed = true }
        instance.io.output.addListener(listener)
        while (!changed) {
            Thread.sleep(10)
        }
        instance.io.output.removeListener(listener)
        instance.io.output.value
    }
}

fun main(args: Array<String>) {
    runBlocking {
        evolve(
            initialData = Data(App.instance, Message.StartApp as Message, 0),
            conditions = EvolutionConditions(
                testObject = Pair(Message.StartUp as Message, 0),
                check = {
                    when (it.first) {
                        is Message.StoppedApp -> false
                        else -> true
                    } && it.second < 100
                },
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
                // just wait
                is Message.Wait,
                is Message.Empty,
                is Message.StartUp,
                is Message.UpdatedApp,
                is Message.StoppedApp -> data.app.waiting(Data(data.app, Message.Wait, data.cnt))
            }
        }
        println("App stopped ... exiting System")
        System.exit(0)
    }
}

/**
 * Components
 * ====================================================================================================================
 */
class IncrementComponent(val io: IO<Data>) : HBox() {
    val button = Button("Click me !")
    val label = Label()
    init {
        button.action {
            io.output.value = io.input.value.copy(message = Message.ClickedIncButton)
        }

        io.input.addListener{_,_,nv -> label.text = "${nv.cnt}"}

        button.resize(100.0,30.0)
        button.minHeight = 30.0
        label.resize(100.0,30.0)
        label.minHeight = 30.0
        children.addAll(button, label)

    }
}

class StopAppComponent(private val io: IO<Data>) : HBox(){
    private val button = Button("Stop")
    private val spacer = Label(" ")
    init {
        button.action {
            io.output.value = io.input.value.copy(message = Message.StopApp)
        }
        button.minWidth = 100.0
        button.minHeight = 30.0
        button.resize(100.0,30.0)
        spacer.minWidth = 100.0
        spacer.resize(100.0,height)
        children.addAll(spacer,button)
    }
}

class AppContent(io : IO<Data>) : VBox() {
    init {
        val spacer = Label()
        spacer.minHeight = 140.0
        children.addAll(
            IncrementComponent(io),
            spacer,
            StopAppComponent(io)
        )
    }
}


