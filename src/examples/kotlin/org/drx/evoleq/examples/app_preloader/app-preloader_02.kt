

package org.drx.evoleq.examples.app_preloader

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.examples.app_preloader.preloader.PreLoader
import org.drx.evoleq.examples.app_preloader.preloader.PreloaderKey
import org.drx.evoleq.examples.app_preloader.preloader.preLoader
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.appStubConfiguration
import org.drx.evoleq.examples.application.dsl.entry
import org.drx.evoleq.examples.application.dsl.fxAppConfiguration
import org.drx.evoleq.examples.application.fx.*
import org.drx.evoleq.examples.application.message.*
import org.drx.evoleq.time.WaitForProperty
import org.drx.evoleq.time.waitForValueToBeSet
import tornadofx.action
import kotlin.reflect.KClass

data class AppData(
    val appStub: ApplicationStub<AppData> = InitAppStub(),
    val message: Message,
    val stubs: HashMap<KClass<*>, Stub<*>> = HashMap(),
    val battery: HashMap<KClass<*>, Message> = HashMap()
)
data class  Update(val value: Int = 0): Message()

class MainSceneKey

/**
 * Stub keys
 */
class PreLoaderStubKey
class MainStageStubKey
class SetupContentRequest : Message()
class SetupContentResponse : Message()

val appConfig2 = fxAppConfiguration<AppData> {
    stubConfig = appStubConfiguration {
        registerConfigurations(
            entry {
                key = PreloaderKey::class
                config = preLoader<Message> {

                    stub = {m -> when(m){
                        is Update-> Immediate{
                                Platform.runLater{
                                    if(label != null) {
                                        label?.text = "${m.value}"
                                    }
                                }
                                m.copy(value = m.value+1)
                        }
                        else -> Immediate{m}
                    }}
                    val pane = Pane()
                    scene = Scene(pane, 800.0,400.0)
                    title = "PreLoader"

                    //style = StageStyle.TRANSPARENT
                    //isMaximized = true
                    isResizable = true
                    //isFullScreen = true

                    x = 300.0
                    y = 100.0
                }
            },
            entry{
                key = MainSceneKey::class
                config = fxStageConfiguration<Message, FxStageConfiguration<Message>> {
                    val out = SimpleObjectProperty<Message>()

                    val content = VBox()
                    scene = Scene(content, 400.0,600.0)
                    x = 300.0
                    y = 100.0

                    content.children.add(Text("Hi there, I am the main scene"))

                    stub = {m -> when (m) {
                        is SetupContentRequest -> {
                            Platform.runLater {
                                val closeButton = Button("Close")
                                closeButton.action {
                                    out.value = FxStop
                                }

                                content.children.add(closeButton)
                            }
                            Immediate{SetupContentResponse()}
                        }
                        else ->  Parallel{WaitForProperty(out).toChange().get()}
                    }}
                }
            }
        )


        stub = {appData ->
            val message = appData.message
            when(message) {
                // Requests
                is FxRequestMessage -> when(message) {
                    is FxInit, FxStart, FxStop, FxLaunch -> Immediate{appData}
                    // Show stage
                    is FxShowStage<*> -> {
                        val show = FxApp.CONFIGURATIONS.get<ShowStageFunction>().configure() as (Stage) -> Unit
                        val register =
                            FxApp.CONFIGURATIONS.get<RegisterStageFunction>().configure() as (KClass<*>, Stage) -> Unit
                        //println("show stage: ${message.key}")
                        when (message.key) {

                            is PreloaderKey -> {
                                val conf = FxApp.CONFIGURATIONS.get(PreloaderKey())
                                var stage: PreLoader<Message>? = null
                                Platform.runLater {

                                    stage = conf.configure() as PreLoader<Message>
                                    register(PreloaderKey::class, stage!!)
                                    show(stage!!)
                                }
                                delay(100)
                                Immediate {
                                    appData.copy(
                                        message = FxShowStageResponse(
                                            PreloaderKey(),
                                            stage!! as Stub<Message>
                                        )
                                    )
                                }
                            }
                            is MainSceneKey -> {
                                val conf = FxApp.CONFIGURATIONS.get(MainSceneKey())
                                val stageProperty = SimpleObjectProperty<Stage>()
                                Platform.runLater {
                                    val stage = conf.configure() as Stage
                                    stageProperty.value = stage
                                    register(MainSceneKey::class, stage)
                                    show(stage)
                                }

                                Immediate {
                                    appData.copy(
                                        message = FxShowStageResponse(
                                            MainSceneKey(),
                                            WaitForProperty(stageProperty).toChange().get() as Stub<Message>
                                        )
                                    )
                                }
                            }
                            else -> {
                                Immediate { appData.copy(
                                    message = NotSupported(receivedMessage = message)
                                )}
                            }
                        }
                    }

                    // Closestage
                    is FxCloseStage<*> -> {
                        val close = FxApp.CONFIGURATIONS.get<CloseStageFunction>().configure() as (Stage) -> Unit
                        val unregister =
                            FxApp.CONFIGURATIONS.get<UnregisterStageFunction>().configure() as (KClass<*>) -> Stage
                        //println("show stage: ${message.key}")
                        when (message.key) {
                            is PreloaderKey -> Immediate {
                                close(unregister(PreloaderKey::class))
                                appData.copy(message = FxCloseStageResponse<PreloaderKey, Message>(PreloaderKey()))
                            }
                            else -> {
                                Immediate { appData }
                            }
                        }
                    }
                }
                else -> {
                    Immediate{appData}
                }
            }
        }
    }
}



fun main() {
    runBlocking {

        val appFlow = suspendedFlow<AppData,Boolean> {
            conditions = conditions {
                testObject = true
                check = {b -> b}
                updateCondition = {appData -> !(appData.message is FxStop)}
            }
            flow = {appData ->
                val message = appData.message
                println("@appFlow: $message")
                when(message) {
                    // Requests
                    is FxRequestMessage -> when (message) {
                        // Launch
                        is FxLaunch -> Immediate {
                            val appStub =
                                GlobalScope.async { launchFxAppFlow(appConfig2).evolve(InitAppStub()).get() }.await()
                            val data = appData.copy(
                                appStub = appStub,
                                message = FxLaunchResponse
                            )
                            data
                        }
                        is FxInit -> Immediate {
                            println("initializing ... ")
                            val s = appData.stubs[PreLoaderStubKey::class]!! as Stub<Message>
                            IntRange(1, 99).forEach {
                                val m = s.stub(Update(it)).get()
                                //println("@appFlow.preLoader: $m")
                                delay(100)
                            }
                            //delay(5_000)
                            println("initializing done")
                            appData.copy(message = FxInitResponse)
                        }
                        is FxStart,
                        is FxStop,
                        is FxShowStage<*>,
                        is FxCloseStage<*> -> Immediate { appData }
                    }
                    // Responses
                    is FxResponseMessage -> when (message) {
                        is FxLaunchResponse ->
                            appData.appStub.stub(appData.copy(message = FxShowStage(PreloaderKey())))

                        is FxInitResponse ->
                            appData.appStub.stub(appData.copy(message = FxShowStage(MainSceneKey())))

                        is FxShowStageResponse<*, *> -> when (message.key) {
                            is PreloaderKey -> Immediate {
                                appData.stubs[PreLoaderStubKey::class] = message.stub
                                appData.copy(message = FxInit)
                            }
                            is MainSceneKey -> {
                                appData.stubs[MainStageStubKey::class] = message.stub
                                //GlobalScope.launch {
                                    coroutineScope {
                                        val stub = message.stub as Stub<Message>

                                        appData.battery[MainStageStubKey::class] =
                                                stub.stub(SetupContentRequest()).get() as SetupContentResponse
                                    }
                                //}
                                appData.appStub.stub(appData.copy(message = FxCloseStage(PreloaderKey())))
                            }

                            else -> Immediate { appData }
                        }

                        // Close stage responses
                        is FxCloseStageResponse<*, *> -> when (message.key) {
                            is PreloaderKey -> Immediate {
                                appData.stubs.remove(PreloaderKey::class)
                                /*
                                val m1 = async{
                                    var m: Message? = null
                                    while (m == null) {
                                        m = appData.battery[MainStageStubKey::class]
                                        appData.battery.remove(MainStageStubKey::class)
                                        delay(1)
                                    }
                                    m!!}.await()
                                    */
                                val m = appData.battery.waitForValueToBeSet(MainStageStubKey::class).get()
                                appData.battery.remove(MainStageStubKey::class)
                                appData.copy(
                                    message = DriveStub(
                                        appData.stubs[MainStageStubKey::class] as Stub<Message>,
                                        m
                                    )
                                )
                            }
                            else -> Immediate { appData }
                        }
                        is FxStartResponse, is FxStopResponse -> Immediate { appData }
                    }

                    is DriveStub<*> -> when (message.initialData) {
                        is SetupContentResponse -> Parallel {
                            val stub = message.stub as Stub<Message>
                            val m = stub.stub(EmptyMessage).get()
                            appData.copy(message = m)
                        }
                        else-> Immediate{ appData.copy(message = FxStop ) }
                    }


                    // other
                    else ->Immediate{
                        //delay(10_000)
                        appData.copy(message = FxStop)
                    }
                }
            }
        }

        val m = appFlow.evolve(AppData(message = FxLaunch)).get()

        //println(m)
        System.exit(0)
    }
}

