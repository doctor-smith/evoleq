/**
 * Copyright (C) 2018 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.examples.app_preloader

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.app_preloader.preloader.PreLoader
import org.drx.evoleq.examples.app_preloader.preloader.PreloaderKey
import org.drx.evoleq.examples.app_preloader.preloader.preLoader
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.appStubConfiguration
import org.drx.evoleq.examples.application.dsl.entry
import org.drx.evoleq.examples.application.dsl.fxAppConfiguration
import org.drx.evoleq.examples.application.fx.FxApp
import org.drx.evoleq.examples.application.fx.ShowStageFunction
import org.drx.evoleq.examples.application.fx.launchFxAppFlow
import org.drx.evoleq.examples.application.message.*


val appConfig = fxAppConfiguration<Message> {
    stubConfig = appStubConfiguration {
        registerConfigurations(
            entry {
                key = PreloaderKey::class
                config = preLoader<Message> {
                    //val p = object : PreLoader<Message>() {
                    stub = {m -> Immediate{m}}
                    scene = Scene(Pane(), 200.0,200.0)
                    title = "PreLoader"
                    //style = StageStyle.TRANSPARENT
                    //isMaximized = true
                    isResizable = true

                    x = 1000.0
                    y = 50.0
                    }
                }
        )


        stub = {
            message -> when(message) {
                is FxShowStage<*> -> {
                    val show = FxApp.CONFIGURATIONS.get<ShowStageFunction>().configure() as (Stage) -> Unit
                    //println("${message.key}")
                    when (message.key){

                        is PreloaderKey ->{
                            val conf = FxApp.CONFIGURATIONS.get(PreloaderKey())
                            var stage: PreLoader<Message>? = null
                            Platform.runLater {

                                stage = conf.configure() as PreLoader<Message>

                                show(stage!!)
                            }
                            delay(5_000)
                            Immediate{ FxShowStageResponse(PreloaderKey(), stage!! as Stub<Message>) }
                        }
                        else ->{
                            Immediate{message}
                        }
                    }
                }
                is FxShowStageResponse<*,*> -> {
                    when(message.key) {
                        is PreloaderKey -> {
                            delay(2_000)
                            Immediate{FxStop}
                        }
                        else -> {Immediate{message}}
                    }
                }
                else -> {
                    Immediate{message}
                }
            }
        }
    }
}



fun main() {
    runBlocking {
        var appStub: ApplicationStub<Message> =InitAppStub()// launchFxAppFlow(appConfig).evolve(InitAppStub()).get()

        val flow = suspendedFlow<Message,Boolean> {
            conditions = conditions {
                testObject = true
                check = {b -> b}
                updateCondition = {message: Message -> !(message is FxStop)}
            }
            flow = {message: Message -> when(message){
                is FxLaunch -> {
                    appStub = launchFxAppFlow(appConfig).evolve(InitAppStub()).get()
                    appStub.stub(FxShowStage(PreloaderKey()))
                }
                else ->appStub.stub(message)}}
        }

        val m = flow.evolve(FxLaunch).get()

        println(m)
        System.exit(0)
    }
}