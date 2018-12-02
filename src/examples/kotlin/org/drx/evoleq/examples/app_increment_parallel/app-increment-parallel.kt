package org.drx.evoleq.examples.app_increment_parallel

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.stage.Screen
import javafx.stage.Stage
import javafx.stage.StageStyle
import kotlinx.coroutines.*
import org.drx.evoleq.*
import tornadofx.ChangeListener
import tornadofx.action

data class AppData(
    val app: IApp<AppData>,
    val message: Message,
    val cnt: Int
)
data class Clock(val time: Long)

data class Data(val appData:AppData, val clock: Clock)

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

interface IApp<D> {
    fun startApp(data: D): Evolving<D>
    fun stopApp(data: D): Evolving<D>
    fun updateApp(data: D): Evolving<D>
    fun waiting(data: D): Evolving<D>
}

class App : tornadofx.App(), IApp<AppData> {
    private object Holder { val INSTANCE = App() }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
    }
    private val out = SimpleObjectProperty<AppData>()
    private val input = SimpleObjectProperty<AppData>()



    override fun init() {
        instance.out.value = instance.input.value.copy(message = Message.InitializingApp)
    }
    override fun start(stage: Stage) {
        stage.initStyle(StageStyle.UNDECORATED)
        val visualBounds = Screen.getPrimary().visualBounds
        val width = visualBounds.width-400
        val height = visualBounds.height-400
        val scene = Scene(FlowPane(),width,height)
        stage.scene = scene
        stage.title = "Example Application"
        val button = Button("CLick me!")
        button.action{
            instance.out.value = instance.input.value.copy(message = Message.ClickedIncButton)
        }
        val label = Label("0")
        instance.input.addListener{_,_,nv -> label.text = "${nv.cnt}"}
        val stop = Button("Stop")
        stop.action {
            instance.out.value = instance.input.value.copy(message = Message.StopApp)
        }

        (scene.root as FlowPane).children.addAll(
            button,
            label,
            stop
            //,restart
        )
        instance.out.value = instance.input.value.copy(message = Message.StartedApp)
        Platform.setImplicitExit(false);
        stage.show()
    }

    override fun stop() {
        Platform.exit()
    }
    override fun startApp(appData: AppData): Evolving<AppData> = Parallel {
        GlobalScope.launch {
            input.value = appData
            launch(App::class.java)
        }
        AppData(this@App,Message.LaunchingApp,appData.cnt)
    }

    override fun updateApp(appData: AppData): Evolving<AppData> = Parallel {
        Platform.runLater {
            instance.input.value = appData
        }
        AppData(this@App,Message.UpdatedApp,appData.cnt)
    }

    override fun stopApp(appData: AppData): Evolving<AppData> = Parallel {
        var running = true
        Platform.runLater {
            stop()
            running = false
        }
        while(running) {
            delay(10)
        }
        AppData(this@App,Message.StoppedApp,appData.cnt)
    }

    override fun waiting(appData: AppData): Evolving<AppData> = Parallel {
        changes().get()
    }

    private fun  changes(): Evolving<AppData> =
        Parallel {
            var changed = false
            val listener = ChangeListener<AppData> { _, _, nv ->  changed = true }
            instance.out.addListener(listener)
            while (!changed) {
                delay(10)
            }
            instance.out.removeListener(listener)
            instance.out.value
        }

}

fun main(args: Array<String>) {
    runBlocking {
        //Parallel<Data>{

        evolve(
            initialData = Data(
                appData = AppData(
                    app = App.instance,
                    message = Message.StartApp,
                    cnt = 0
                ),
                clock = Clock(0L)
            ),
            conditions = EvolutionConditions<Data,Pair<Message,Long>>(
                testObject = Pair(Message.StartUp, 0),
                check = { when(it.first) {
                    is Message.StoppedApp -> false
                    else -> true} && it.second < 30
                },
                updateCondition = { data -> Pair(data.appData.message, data.clock.time) }
            )
        ){  data -> Parallel {
                val appData= Parallel {
                    evolve(
                        initialData = data.appData,
                        conditions = EvolutionConditions(
                            testObject = Pair(Message.StartUp as Message, 0),
                            check = { when(it.first) { Message.StoppedApp -> false else -> true} && it.second < 100 },
                            updateCondition = { data -> Pair(data.message, data.cnt) }
                        )
                    ){  data -> println("App driver: "+Thread.currentThread().name); println(data.message)
                        when (data.message) {
                            Message.StartApp -> data.app.startApp(data)
                            //"restart" -> data.app.restartApp(data)
                            Message.LaunchingApp ->data.app.waiting(AppData(data.app, Message.Wait, data.cnt))
                            Message.InitializingApp -> data.app.waiting(AppData(data.app, Message.Wait, data.cnt))
                            Message.StartedApp -> data.app.updateApp(AppData(data.app, Message.Empty, data.cnt))
                            Message.ClickedIncButton -> data.app.updateApp(AppData(data.app, Message.Empty, data.cnt + 1))
                            Message.StopApp -> data.app.stopApp(AppData(data.app, Message.Empty, data.cnt))
                            // just wait
                            Message.Wait,
                            Message.Empty,
                            Message.StartUp,
                            Message.UpdatedApp,
                            Message.StoppedApp -> data.app.waiting(AppData(data.app, Message.Wait, data.cnt))
                        }
                    }
                }
                val clock = Parallel {
                    evolve(
                        initialData = data.clock,
                        conditions = EvolutionConditions(
                            testObject = 0L,
                            check = {time -> time < 5},
                            updateCondition = {clock -> clock.time}
                        )
                    ){  clock -> Parallel {
                            println("Clock: "+Thread.currentThread().name)
                            println("Clock.time: ${clock.time}")
                            delay(1_000)
                            clock.copy( time = clock.time+1 )
                        }
                    }
                }
                Data( appData.get(), clock.get() )
            }
        }
    }
    System.exit(0)
}





