package org.drx.evoleq.examples.app_increment_parallel

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

data class AppData(
    val app: IApp<AppData>,
    val message: String,
    val cnt: Int
)
data class Clock(val time: Long)

data class Data(val appData:AppData, val clock: Clock)

/**
 * TODO AppIO Monad
 */

class App : tornadofx.App(), IApp<AppData> {
    private object Holder { val INSTANCE = App() }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
    }
    private val out = SimpleObjectProperty<AppData>()
    private val input = SimpleObjectProperty<AppData>()



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
        val restart = Button("Restart")
        restart.action {
            instance.out.value = instance.input.value.copy(message = "restart")
        }
        (scene.root as FlowPane).children.addAll(
            button,
            label,
            stop,
            restart
        )
        instance.out.value = instance.input.value.copy(message = "started")
        Platform.setImplicitExit(false);
        stage.show()
    }

    override fun stop() {
        Platform.exit()
    }
    override fun startApp(appData: AppData): Evolving<AppData> = Parallel {
        GlobalScope.launch {
            input.value = appData
            val x = launch(App::class.java)
        }
        AppData(this@App,"launching-app",appData.cnt)
    }

    override fun updateApp(appData: AppData): Evolving<AppData> = Parallel {
        Platform.runLater {
            instance.input.value = appData
        }
        AppData(this@App,"updated",appData.cnt)
    }

    override fun stopApp(appData: AppData): Evolving<AppData> = Parallel {
        stop()
        AppData(this@App,"stopped",appData.cnt)
    }

    override fun waiting(appData: AppData): Evolving<AppData> = Parallel {
        changes().get()
    }

    override fun restartApp(appData: AppData): Evolving<AppData> = Parallel {
        val cnt = appData.cnt
        stop()
        delay(5_000)
        AppData( App(),"start-app", cnt )
    }

    private fun  changes(): Evolving<AppData> = Parallel {
        var m:AppData
        var changed = false
        val listener = ChangeListener<AppData> { _, _, nv -> m = nv; changed = true }
        instance.out.addListener(listener)
        while(!changed){
            delay(10)
        }
        instance.out.removeListener(listener)
        instance.out.value
    }
}

fun main(args: Array<String>) {
    runBlocking {
        evolve(
            initialData = Data(
                appData = AppData(
                    app = App.instance,
                    message = "start-app",
                    cnt = 0
                ),
                clock = Clock(0L)
            ),
            conditions = EvolutionConditions<Data,Pair<String,Long>>(
                testObject = Pair("startup", 0),
                check = { it.first != "stopped" && it.second < 30 },
                updateCondition = { data -> Pair(data.appData.message, data.clock.time) }
            )
        ){  data -> Parallel {
                val deferredAppData= Parallel {
                    evolve(
                        initialData = data.appData,
                        conditions = EvolutionConditions(
                            testObject = Pair("startup", 0),
                            check = { it.first != "stopped" && it.second < 100 },
                            updateCondition = { data -> Pair(data.message, data.cnt) }
                        )
                    ){  data -> println("App driver: "+Thread.currentThread().name); println(data.message)
                        when (data.message) {
                            "start-app" -> data.app.startApp(data)
                            "restart" -> data.app.restartApp(data)
                            "launching-app" ->data.app.waiting(AppData(data.app, "", data.cnt))
                            "initializing" -> data.app.waiting(AppData(data.app, "", data.cnt))
                            "started" -> data.app.updateApp(AppData(data.app, "", data.cnt))
                            "clicked" -> data.app.updateApp(AppData(data.app, "", data.cnt + 1))
                            "stop" -> data.app.stopApp(AppData(data.app, "", data.cnt))
                            else -> data.app.waiting(AppData(data.app, "", data.cnt))
                        }
                    }
                }
                val deferredClock = Parallel {
                    evolve(
                        initialData = data.clock,
                        conditions = EvolutionConditions(
                            testObject = 0L,
                            check = {time -> time < 50},
                            updateCondition = {clock -> clock.time}
                        )
                    ){  clock -> Parallel {
                            println("Clock: "+Thread.currentThread().name)
                            println("Clock.time: ${clock.time}")
                            delay(1_000)
                            clock.copy(time = clock.time+1)
                        }
                    }

                }
                Data( deferredAppData.get(), deferredClock.get() )
            }
        }
    }
}



interface IApp<D> {
    fun startApp(data: D): Evolving<D>
    fun stopApp(data: D): Evolving<D>
    fun updateApp(data: D): Evolving<D>
    fun waiting(data: D): Evolving<D>
    fun restartApp(data: D): Evolving<D>
}

