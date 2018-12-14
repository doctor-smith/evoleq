

package org.drx.evoleq.examples.app_preloader

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import kotlinx.coroutines.*
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.app_preloader.preloader.PreLoader
import org.drx.evoleq.examples.app_preloader.preloader.PreloaderKey
import org.drx.evoleq.examples.app_preloader.preloader.preLoader
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.InitStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.appStubConfiguration
import org.drx.evoleq.examples.application.dsl.entry
import org.drx.evoleq.examples.application.dsl.fxAppConfiguration
import org.drx.evoleq.examples.application.fx.*
import org.drx.evoleq.examples.application.message.*
import kotlin.reflect.KClass

data class AppData(
    val appStub: ApplicationStub<AppData> = InitAppStub(),
    val message: Message,
    val stubs: HashMap<KClass<*>, Stub<*>> = HashMap()
)
data class  Update(val value: Int = 0): Message()

class MainSceneKey

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
                    stub = {m -> Immediate{m}}
                    scene = Scene(Pane(), 100.0,100.0)
                }
            }
        )


        stub = {appData ->
            val message = appData.message
            when(message) {
                is FxShowStage<*> -> {
                    val show = FxApp.CONFIGURATIONS.get<ShowStageFunction>().configure() as (Stage) -> Unit
                    val register = FxApp.CONFIGURATIONS.get<RegisterStageFunction>().configure() as (KClass<*>,Stage) -> Unit
                    //println("show stage: ${message.key}")
                    when (message.key){

                        is PreloaderKey ->{
                            val conf = FxApp.CONFIGURATIONS.get(PreloaderKey())
                            var stage: PreLoader<Message>? = null
                            Platform.runLater {

                                stage = conf.configure() as PreLoader<Message>
                                register(PreloaderKey::class,stage!!)
                                show(stage!!)
                            }
                            delay(100)
                            Immediate{ appData.copy(message = FxShowStageResponse(PreloaderKey(), stage!! as Stub<Message>)) }
                        }
                        is MainSceneKey -> {
                            val conf = FxApp.CONFIGURATIONS.get(MainSceneKey())
                            Platform.runLater {
                                val stage = conf.configure() as Stage
                                register(MainSceneKey::class,stage)
                                show(stage)
                            }
                            Immediate{ appData.copy(message = FxShowStageResponse<MainSceneKey,Message>(MainSceneKey())) }
                        }
                        else ->{
                            Immediate{appData}
                        }
                    }
                }
                is FxCloseStage<*> -> {
                    val close = FxApp.CONFIGURATIONS.get<CloseStageFunction>().configure() as (Stage) -> Unit
                    val unregister = FxApp.CONFIGURATIONS.get<UnregisterStageFunction>().configure() as (KClass<*>) -> Stage
                    //println("show stage: ${message.key}")
                    when (message.key){
                        is PreloaderKey -> Immediate{
                            close(unregister(PreloaderKey::class))
                            appData.copy(message = FxCloseStageResponse<PreloaderKey,Message>(PreloaderKey()))
                        }
                        else -> {
                            Immediate{appData}
                        }
                    }
                }
                is FxShowStageResponse<*,*> -> {
                    when(message.key) {
                        is PreloaderKey -> {
                            //delay(2_000)
                            Immediate{ appData.copy(message = FxInit) }
                        }
                        else -> {
                            Immediate{appData}
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
                when(message){
                    is FxLaunch -> Immediate{
                        val appStub = GlobalScope.async {  launchFxAppFlow(appConfig2).evolve(InitAppStub()).get() }.await()
                         val data = appData.copy(
                            appStub = appStub,
                            message = FxLaunchRespose
                        )
                        data
                    }
                    is FxLaunchRespose ->
                        appData.appStub.stub(appData.copy(message = FxShowStage(PreloaderKey())))
                    is FxShowStageResponse<*,*> -> when(message.key) {
                        is PreloaderKey -> Immediate{
                            appData.stubs[PreloaderKey::class] = message.stub
                            appData.copy(message = FxInit)}
                        is MainSceneKey ->
                            appData.appStub.stub(appData.copy(message = FxCloseStage(PreloaderKey())))

                        else -> Immediate{appData}
                    }
                    is FxCloseStageResponse<*,*> -> when(message.key) {
                        is PreloaderKey -> Immediate{
                            appData.stubs.remove(PreloaderKey::class)
                            delay(3_000)
                            appData.copy(message = FxStop)
                        }
                        else -> Immediate{appData}
                    }


                    is FxInit ->  Immediate{
                        println("initializing ... ")
                        val s = appData.stubs[PreloaderKey::class]!! as Stub<Message>
                        IntRange(1,99).forEach {
                            val m= s.stub(Update(it)).get()
                            //println("@appFlow.preLoader: $m")
                            delay(100)
                        }
                        //delay(5_000)
                        println("initializing done")
                        appData.copy(message = FxInitRespose)
                    }

                    is FxInitRespose ->
                        appData.appStub.stub(appData.copy(message = FxShowStage(MainSceneKey())))


                    else ->Immediate{
                        //delay(10_000)
                        appData.copy(message = FxStop)
                    }
                }
            }
        }

        val m = appFlow.evolve(AppData(message = FxLaunch)).get()

        println(m)
        System.exit(0)
    }
}

