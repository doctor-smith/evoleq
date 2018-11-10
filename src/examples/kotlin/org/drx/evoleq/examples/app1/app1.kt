package org.drx.evoleq.examples.app1

import javafx.application.Application
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.FlowPane
import javafx.stage.Screen
import javafx.stage.Stage
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.drx.evoleq.evolve
import tornadofx.App
import tornadofx.action

data class Data(
    val app: IApp<Data>,
    val message: String,
    val cnt: Int
)

class Appl : App(), IApp<Data> {

    private val out = SimpleStringProperty()
    private val input = SimpleStringProperty()

    override fun init() {

        GlobalScope.launch {
            evolve<Data, Pair<String, Int>, String>(
                data = Data(this@Appl, "initializing", 0),
                testObject = Pair("startup", 0),
                condition = { it.first != "stopped" && it.second < 100 },
                updateCondition = { data -> Pair(data.message, data.cnt) },
                flow = { data ->
                    when (data.message) {
                        "initializing" -> data.app.waiting(Data(data.app, "", data.cnt))
                        "started" -> data.app.updateApp(Data(data.app, "", data.cnt))
                        "clicked" -> data.app.updateApp(Data(data.app, "", data.cnt + 1))
                        "stop" -> data.app.stopApp(Data(data.app, "", data.cnt))
                        else -> data.app.waiting(Data(data.app, "", data.cnt))
                    }
                }
            )
        }
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
             out.value = "clicked"
        }
        val label = Label("0")
        label.textProperty().bind(input)
        val stop = Button("Stop")
        stop.action {
            out.value = "stop"
        }
        (scene.root as FlowPane).children.addAll(
            button,
            label,
            stop
        )
        out.value = "started"
        stage.show()
    }

    override fun stop() {
        System.exit(0)
    }
    override fun startApp(data: Data): Deferred<Data> = GlobalScope.async {
        Application.launch(Appl::class.java)
        Data(this@Appl,"started",data.cnt)
    }

    override fun updateApp(data: Data): Deferred<Data> = GlobalScope.async {
        Platform.runLater {
            input.value = "${data.cnt}"
            out.value = ""
        }
        Data(this@Appl,"updated",data.cnt)
    }

    override fun stopApp(data: Data): Deferred<Data> = GlobalScope.async{
        Platform.runLater {
            stop()
        }
        Data(this@Appl,"stopped",data.cnt)
    }

    override fun waiting(data: Data): Deferred<Data> = GlobalScope.async {
        var m = ""

        out.addListener{_,_,nv ->
            m = nv

        }
        while(m == ""){
            Thread.sleep(10)
        }
        Data(this@Appl, m,data.cnt)

    }

}

fun main(args: Array<String>) {
    Application.launch(Appl::class.java, *args)
}

interface IApp<D> {
    fun startApp(data: D): Deferred<D>
    fun stopApp(data: D): Deferred<D>
    fun updateApp(data: D): Deferred<D>
    fun waiting(data: D): Deferred<D>
}

