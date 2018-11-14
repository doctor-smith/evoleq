package org.drx.evoleq.examples.app_increment_parallel

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.*
import org.drx.evoleq.evolve
import tornadofx.ChangeListener

import tornadofx.action
import java.lang.Thread.sleep
import kotlin.coroutines.CoroutineContext

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
//val out = SimpleStringProperty()
//val input = SimpleStringProperty()

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
        //label.textProperty().bind(instance.input)
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
        Platform.setImplicitExit(false);
        stage.show()
    }

    override fun stop() {
        Platform.exit()
    }
    override fun startApp(appData: AppData): Deferred<AppData> = GlobalScope.async {
        GlobalScope.launch {
            input.value = appData
            val x = Application.launch(App::class.java)
        }
        AppData(this@App,"launching-app",appData.cnt)
    }

    override fun updateApp(appData: AppData): Deferred<AppData> = GlobalScope.async {
        Platform.runLater {
            instance.input.value = appData
            //instance.out.value = null
        }
        AppData(this@App,"updated",appData.cnt)
    }

    override fun stopApp(appData: AppData): Deferred<AppData> = GlobalScope.async{
        stop()
        AppData(this@App,"stopped",appData.cnt)
    }

    override fun waiting(appData: AppData): Deferred<AppData> = GlobalScope.async {
        //AppData(this@App, changes().await() ,appData.cnt)
        changes().await()
    }

    private fun  changes(): Deferred<AppData> = GlobalScope.async {
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
        //val terminalData =
        evolve<Data,Pair<String,Long>,Int>(
            data = Data(AppData(App.instance, "start-app", 0),Clock(0L)),
            testObject = Pair("startup", 0),
            condition = { it.first != "stopped" && it.second < 30 },
            updateCondition = { data -> Pair(data.appData.message, data.clock.time) },
            flow = {  data ->  async {
                    println("Top level: "+Thread.currentThread().name)
                    val defAppData= async{
                        evolve<AppData, Pair<String, Int>, String>(
                            data = data.appData,
                            testObject = Pair("startup", 0),
                            condition = { it.first != "stopped" && it.second < 100 },
                            updateCondition = { data -> Pair(data.message, data.cnt) },
                            flow = { data ->
                                println("App driver: "+Thread.currentThread().name)
                                println(data.message)
                                when (data.message) {
                                    "start-app" -> data.app.startApp(data)
                                    "launching-app" ->data.app.waiting(AppData(data.app, "", data.cnt))
                                    "initializing" -> data.app.waiting(AppData(data.app, "", data.cnt))
                                    "started" -> data.app.updateApp(AppData(data.app, "", data.cnt))
                                    "clicked" -> data.app.updateApp(AppData(data.app, "", data.cnt + 1))
                                    "stop" -> data.app.stopApp(AppData(data.app, "", data.cnt))

                                    else -> data.app.waiting(AppData(data.app, "", data.cnt))
                                }
                            }
                        )
                    }
                    val defClock = async{
                        evolve<Clock,Long,Long>(
                            data = data.clock,
                            testObject = 0L,
                            condition = {time -> time < 5},
                            updateCondition = {clock -> clock.time},
                            flow = { clock -> async {
                                println("Clock: "+Thread.currentThread().name)
                                println("Clock.time: ${clock.time}")
                                delay(1_000)
                                clock.copy(time = clock.time+1)
                            }}
                        )
                    }
                Data(defAppData.await(),defClock.await())
                }
            }
        )



    }
}



interface IApp<D> {
    fun startApp(data: D): Deferred<D>
    fun stopApp(data: D): Deferred<D>
    fun updateApp(data: D): Deferred<D>
    fun waiting(data: D): Deferred<D>
}

