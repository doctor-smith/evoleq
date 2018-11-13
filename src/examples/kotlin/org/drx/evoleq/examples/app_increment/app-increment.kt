package org.drx.evoleq.examples.app_increment

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
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

data class Data(
    val app: IApp<Data>,
    val message: String,
    val cnt: Int
)

/**
 * TODO AppIO Monad
 */
//val out = SimpleStringProperty()
//val input = SimpleStringProperty()

class App : tornadofx.App(), IApp<Data> {
    private object Holder { val INSTANCE = App() }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
    }
    private val out = SimpleStringProperty()
    private val input = SimpleObjectProperty<Data>()

    override fun init() {
        instance.out.value = "initializing"
        /*
        GlobalScope.launch {
            evolve<Data, Pair<String, Int>, String>(
                data = Data(App(), "start-app", 0),
                testObject = Pair("startup", 0),
                condition = { it.first != "stopped" && it.second < 100 },
                updateCondition = { data -> Pair(data.message, data.cnt) },
                flow = { data ->
                    when (data.message) {
                        "start-app" -> data.app.startApp(data)
                        "initializing" -> data.app.waiting(Data(data.app, "", data.cnt))
                        "started" -> data.app.updateApp(Data(data.app, "", data.cnt))
                        "clicked" -> data.app.updateApp(Data(data.app, "", data.cnt + 1))
                        "stop" -> data.app.stopApp(Data(data.app, "", data.cnt))
                        else -> data.app.waiting(Data(data.app, "", data.cnt))
                    }
                }
            )
        }
        */
    }
    override fun start(stage: Stage) {
        val visualBounds = Screen.getPrimary().getVisualBounds()
        val width = visualBounds.width-400
        val height = visualBounds.height-400
        val scene = Scene(FlowPane(),width,height)
        stage.scene = scene
        stage.title = "Example Application"
        val button = Button("CLick me!")
        button.action{
            instance.out.value = "clicked"
        }
        val label = Label("0")
        instance.input.addListener{_,_,nv -> label.text = "${nv.cnt}"}
        //label.textProperty().bind(instance.input)
        val stop = Button("Stop")
        stop.action {
            instance.out.value = "stop"
        }
        (scene.root as FlowPane).children.addAll(
            button,
            label,
            stop
        )
        instance.out.value = "started"
        stage.show()
    }

    override fun stop() {
        System.exit(0)
    }
    override fun startApp(data: Data): Deferred<Data> = GlobalScope.async {
        GlobalScope.launch {
            val x = Application.launch(App::class.java)
        }
        Data(this@App,"launching-app",data.cnt)
    }

    override fun updateApp(data: Data): Deferred<Data> = GlobalScope.async {
        Platform.runLater {
            instance.input.value = data
            instance.out.value = ""
        }
        Data(this@App,"updated",data.cnt)
    }

    override fun stopApp(data: Data): Deferred<Data> = GlobalScope.async{
        Platform.runLater {
            stop()
        }
        Data(this@App,"stopped",data.cnt)
    }

    override fun waiting(data: Data): Deferred<Data> = GlobalScope.async {
        Data(this@App, changes().await() ,data.cnt)
    }

    private fun  changes(): Deferred<String> = GlobalScope.async {
        var m:String = "NULL"
        val listener = ChangeListener<String> {_,_,nv -> m = nv }
        instance.out.addListener(listener)
        while(m == "NULL"){
            Thread.sleep(10)
        }
        instance.out.removeListener(listener)
        m
    }
}

fun main(args: Array<String>) {
    runBlocking {
        //GlobalScope.launch {
            evolve<Data, Pair<String, Int>, String>(
                data = Data(App.instance, "start-app", 0),
                testObject = Pair("startup", 0),
                condition = { it.first != "stopped" && it.second < 100 },
                updateCondition = { data -> Pair(data.message, data.cnt) },
                flow = { data ->
                    println(data.message)
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
            )
        //}
    }
    //Application.launch(App::class.java, *args)
}



interface IApp<D> {
    fun startApp(data: D): Deferred<D>
    fun stopApp(data: D): Deferred<D>
    fun updateApp(data: D): Deferred<D>
    fun waiting(data: D): Deferred<D>
}

