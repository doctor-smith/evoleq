/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.examples.app_filesystem.fx

import javafx.stage.Stage
import kotlinx.coroutines.*
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.appStubConfiguration
import org.drx.evoleq.examples.application.dsl.fxAppConfiguration
import org.drx.evoleq.examples.application.fx.*
import org.drx.evoleq.examples.application.message.*
import kotlin.reflect.KClass


class AppStubKey
val appConf = fxAppConfiguration<Message> {
    stubConfig = appStubConfiguration {
        stub = { message ->
            //require(message is ConfigRequestMessage  || message is FxRequestMessage)
            println("@fxAppStub: $message")
            when (message) {
                is ConfigRequestMessage -> when(message){
                    is RegisterConfigurations -> {
                        message.entries.forEach {
                            FxApp.FX_CONFIGURATIONS.registry[it.key] = it.config
                        }
                        Immediate { RegisteredConfigurations }
                    }
                    is RegisterConfiguration -> {
                        FxApp.FX_CONFIGURATIONS.registry[message.entry.key] = message.entry.config
                        Immediate{ RegisteredConfiguration }
                    }
                }
                is FxRequestMessage -> when (message) {
                    is FxShowStage<*> -> {
                        val show = FxApp.CONFIGURATIONS.get<ShowStageFunction>().configure() as (Stage) -> Unit
                        val register =
                            FxApp.CONFIGURATIONS.get<RegisterStageFunction>().configure() as (KClass<*>, Stage) -> Unit
                        when (message.key) {

                            is MainStageKey -> {
                                println("@fxAppStub.fxShowStage mainStageKey")
                                var stage: Stage? = null
                                var conf = FxApp.FX_CONFIGURATIONS.get(MainStageKey())

                                stage = conf.configure().get() as Stage
                                delay(1000)
                                register(MainStageKey::class, stage!!)
                                show(stage!!)
                                Immediate {
                                    while (stage == null ){//|| nScene == null) {
                                        println("stage is null ...")
                                        delay(1000)
                                    }
                                    FxShowStageResponse(MainStageKey(), stage as Stub<Message>)
                                }
                            }
                            is AddFileDialogKey-> {
                                var stage: Stage? = null
                                var conf = FxApp.FX_CONFIGURATIONS.get(AddFileDialogKey())

                                stage = conf.configure().get() as Stage
                                //delay(1000)
                                register(AddFileDialogKey::class, stage!!)
                                show(stage!!)
                                Immediate{FxShowStageResponse(AddFileDialogKey(),stage as Stub<Message>)}
                            }
                            else -> Immediate { NotSupported(message) }
                        }
                    }
                    is FxCloseStage<*> -> {
                        val close =
                            FxApp.CONFIGURATIONS.get<CloseStageFunction>().configure() as (Stage) -> Unit
                        val unregister =
                            FxApp.CONFIGURATIONS.get<UnregisterStageFunction>().configure() as (KClass<*>) -> Stage
                        when (message.key) {
                            is MainStageKey -> {
                                close(unregister(MainStageKey::class))
                                Immediate { FxCloseStageResponse<MainStageKey, Message>(MainStageKey()) }
                            }
                            is AddFileDialogKey -> {
                                close(unregister(AddFileDialogKey::class))
                                Immediate { FxCloseStageResponse<AddFileDialogKey, Message>(AddFileDialogKey()) }
                            }
                            else -> Immediate { NotSupported(message) }
                        }
                    }
                    is FxLaunch, FxInit, FxStart, FxStop -> Immediate { NotSupported(message) }
                }
                else -> Immediate { message }
            }
        }
    }

}