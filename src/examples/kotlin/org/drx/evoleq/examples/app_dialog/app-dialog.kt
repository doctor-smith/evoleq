package org.drx.evoleq.examples.app_dialog

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
import org.drx.evoleq.*
import org.drx.evoleq.conditions.EvolutionConditions
import org.drx.evoleq.util.tail
import tornadofx.ChangeListener
import tornadofx.action



data class Data(
    val app: IApp<Data>,
    val message: Message,
    val cnt: Int
)

interface IApp<D> {
    fun startApp(data: D): Evolving<D>
    fun restartApp(data: D): Evolving<D>
    fun stopApp(data: D): Evolving<D>
    fun updateApp(data: D): Evolving<D>
    fun waiting(data: D): Evolving<D>
}

interface StageController {
    val stages: HashMap<Long, Stage>
    fun showStage(stage: Stage)
    fun closeStage(stage: Stage)
}

sealed class Message {
    object StartApp : Message()
    object RestartApp : Message()
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
    class Continuation(val blocks: ArrayList<(Data)-> Evolving<Data>> = arrayListOf()) : Message()  {
        constructor(process:(Data)->Evolving<Data>): this(arrayListOf({d:Data -> process(d)}))
    }
    sealed class Dialog(val id: Long) : Message() {
        class Confirm(val statement: String,id: Long,val continuation: Continuation = Continuation()) : Dialog(id)
        class Ok(id: Long,val  continuation: Continuation = Continuation()) : Dialog(id)
        class Cancel(id: Long, val continuation: Continuation = Continuation()) : Dialog(id)
        class Close(id: Long, val  continuation: Continuation = Continuation()): Dialog(id)
    }
}

class IO<D> {
    val output = SimpleObjectProperty<D>()
    val input = SimpleObjectProperty<D>()
}

class App : tornadofx.App(), IApp<Data>, StageController {
    private object Holder {
        val INSTANCE = App()
        val STAGES = HashMap<Long, Stage>()
    }

    companion object {
        val instance: App by lazy { Holder.INSTANCE }
        val stageRegistry: HashMap<Long, Stage> by lazy { Holder.STAGES }
    }

    private val io = IO<Data>()

    /**
     * JavaFX
     * =================================================================================================================
     */
    override fun init() {
        instance.io.output.value = instance.io.input.value.copy(message = Message.InitializingApp)
    }

    override fun start(stage: Stage) {
        stage.initStyle(StageStyle.UNDECORATED)
        //val visualBounds = Screen.getPrimary().visualBounds
        val width = 200.0
        val height = 200.0
        val scene = Scene(AppContent(instance.io), width, height)
        stage.scene = scene
        stage.title = "Example Application"

        instance.io.output.value = instance.io.input.value.copy(message = Message.StartedApp)
        showStage(stage)
    }

    override fun stop() {
        Platform.exit()
    }

    /**
     * StageController
     * =================================================================================================================
     */
    override val stages: HashMap<Long, Stage>
        get() = stageRegistry
    override fun closeStage(stage: Stage) {
        Platform.runLater {
            stage.close()
        }
    }
    override fun showStage(stage: Stage) {
        Platform.runLater{
            stage.show()
        }
    }

    /**
     * IApp
     * =================================================================================================================
     */
    override fun startApp(data: Data): Evolving<Data> = Parallel {
        GlobalScope.launch {
            Thread(
                kotlinx.coroutines.Runnable {
                    println("start app on thread: " + Thread.currentThread().name)
                    io.input.value = data
                    val x = launch(App::class.java)
                    println(">>> Return from launch")
                    //io.output.value = io.input.value.copy(message = )
                }
            ).start()
        }
        Data(this@App, Message.LaunchingApp, data.cnt)
    }

    override fun updateApp(data: Data): Evolving<Data> = Parallel {
        when (data.message) {
            is Message.Dialog.Confirm -> {
                val id = data.message.id
                Platform.runLater {
                    val dialog = ConfirmDialog(data.message, instance.io)
                    stages[id] = dialog

                    showStage(dialog)
                    dialog.toFront()
                }
            }
            is Message.Dialog.Close -> {
                val id = data.message.id
                Platform.runLater {
                    stages[id]?.close()
                    stages.remove(id)
                }
                if(data.message.continuation.blocks.size > 0) {
                    println("Return with continuation after closing dialog ... \n")
                    return@Parallel data.copy(message= data.message.continuation)
                }
            }
        }
        if (data.message is Message.Continuation) {
            println("Return with continuation ... ")
            //println("\n${data.message}\n")
             data
        } else {
            Platform.runLater {
            instance.io.input.value = data
        }
        Data(this@App, Message.UpdatedApp, data.cnt)
        }
    }

    override fun stopApp(data: Data): Evolving<Data> = Parallel {
        var running = true
        Platform.runLater {
            stop()
            running = false
        }
        while(running) {
            delay(10)
        }
        Data(this@App, Message.StoppedApp, data.cnt)
    }

    override fun restartApp(data: Data): Evolving<Data> = Parallel {
        var running = true

        Platform.runLater {
            stop()
            running = false
        }
        //GlobalScope.launch {
        while (running) {
            delay(10)
        }
        instance.init()
        Platform.runLater {
            instance.start(Stage())
        }
        //io.output.value = io.input.value.copy(message = Message.StartedApp)
        Data(this@App, Message.Wait, data.cnt)
    }

    override fun waiting(data: Data): Evolving<Data> = Parallel {
        changes().get()
    }

    private fun changes(): Evolving<Data> = Parallel {
        var m: Data
        var changed = false
        val listener = ChangeListener<Data> { _, _, nv -> m = nv; changed = true }
        instance.io.output.addListener(listener)
        while (!changed) {
            delay(10)
        }
        instance.io.output.removeListener(listener)
        instance.io.output.value
    }
}


/**
 * Main: Evolve
 * =====================================================================================================================
 */
fun main(args: Array<String>) {
    runBlocking {
        evolve<Data,Pair<Message,Int>>(
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
        ) { data ->
            println(data.message)
            when (data.message) {
                // application
                is Message.StartApp -> data.app.startApp(data)
                is Message.RestartApp -> data.app.restartApp(data)
                is Message.LaunchingApp -> data.app.waiting(Data(data.app, Message.Wait, data.cnt))
                is Message.InitializingApp -> data.app.waiting(Data(data.app, Message.Wait, data.cnt))
                is Message.StartedApp -> data.app.updateApp(Data(data.app, Message.Empty, data.cnt))
                // else
                is Message.UpdatedApp,
                is Message.Wait,
                is Message.StartUp,
                is Message.Empty,
                is Message.StoppedApp-> data.app.waiting(Data(data.app, Message.Wait, data.cnt))

                // continuation
                is Message.Continuation -> data.message.blocks.first() (
                    data.copy(message = Message.Continuation(
                        data.message.blocks.tail())
                    )
                )

                // interaction
                is Message.ClickedIncButton -> data.app.updateApp(Data(data.app, Message.Empty, data.cnt + 1))
                is Message.StopApp -> Parallel{
                    val id = System.currentTimeMillis()
                    val continuation = Parallel{
                        process (
                            { d: Data -> d.app.updateApp(d.copy(message = Message.Dialog.Close(id))) },
                            { d: Data -> d.app.stopApp(d.copy(message = Message.Empty)) }
                        )
                    }.get()
                    data.app.updateApp(data.copy(
                    message=Message.Dialog.Confirm(
                        "Do you really want to close the application?",
                        id,
                        Message.Continuation( continuation )
                    ))).get()
                }
                is Message.Dialog.Cancel -> data.app.updateApp(data.copy(message = Message.Dialog.Close(data.message.id)))
                is Message.Dialog.Ok -> data.app.updateApp(data.copy(message = data.message.continuation))
                // else
                is Message.Dialog.Confirm,
                is Message.Dialog.Close -> data.app.waiting(Data(data.app, Message.Wait, data.cnt))
            }
        }
        println("App stopped ... exiting System")
        System.exit(0)
    }
}

/**
 * Components
 * =====================================================================================================================
 */
class IncrementComponent(val io: IO<Data>) : HBox() {
    val button = Button("Click me !")
    val label = Label()

    init {
        button.action {
            io.output.value = io.input.value.copy(message = Message.ClickedIncButton)
        }

        io.input.addListener { _, _, nv -> label.text = "${nv.cnt}" }

        button.resize(100.0, 30.0)
        button.minHeight = 30.0
        label.resize(100.0, 30.0)
        label.minHeight = 30.0
        children.addAll(button, label)

    }
}

class StopAppComponent(private val io: IO<Data>) : HBox() {
    private val button = Button("Stop")
    private val spacer = Button("Restart")

    init {
        button.action {
            io.output.value = io.input.value.copy(message = Message.StopApp)
        }
        button.minWidth = 100.0
        button.minHeight = 30.0
        button.resize(100.0, 30.0)

        spacer.action{
            io.output.value = io.input.value.copy(message = Message.RestartApp)
        }
        spacer.minWidth = 100.0
        spacer.resize(100.0, height)
        children.addAll(spacer, button)
    }
}

class AppContent(io: IO<Data>) : VBox() {
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

class ConfirmDialog(val confirm: Message.Dialog.Confirm ,private val io: IO<Data>) : Stage() {
    init {
        initStyle(StageStyle.UNDECORATED)
        val root = VBox()
        scene = Scene(root, 200.0,100.0)
        val label = Label(confirm.statement)
        val ok = Button("Ok")
        val cancel = Button("cancel")

        cancel.action {
            io.output.value = io.input.value.copy(message = Message.Dialog.Cancel(confirm.id))
        }
        ok.action {
            io.output.value = io.input.value.copy(message = Message.Dialog.Ok(confirm.id,confirm.continuation))
        }

        root.children.addAll(
            label,
            HBox(cancel, ok)
        )
    }
}




/**
 * Auxiliary
 * =====================================================================================================================
 */
/*
fun <T> ArrayList<T>.tail(): ArrayList<T> {
    val N = size

    if(N <= 1){
        //println("tail(): size = ${N-1}")
        return arrayListOf()
    }
    //println("tail(): size = ${N-1}")
    val tail = arrayListOf<T>()
    IntRange(1,N-1).forEach { tail.add(this[it]) }
    return tail
}
        */