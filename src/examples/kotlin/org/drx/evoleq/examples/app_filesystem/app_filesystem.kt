/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
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
package org.drx.evoleq.examples.app_filesystem

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.examples.app_filesystem.data.RootFolder
import org.drx.evoleq.examples.app_filesystem.fx.*
import org.drx.evoleq.examples.app_filesystem.message.*
import org.drx.evoleq.examples.app_filesystem.stubs.FileSystemStub
import org.drx.evoleq.examples.app_filesystem.stubs.FileSystemStubKey
import org.drx.evoleq.examples.app_preloader.MainStageStubKey
import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.fxEntry
import org.drx.evoleq.examples.application.fx.launchFxAppFlow
import org.drx.evoleq.examples.application.message.*
import org.drx.evoleq.time.waitForValueToBeSet
import kotlin.reflect.KClass

data class Data(
    val stubs: HashMap<KClass<*>, Stub<*>>  = HashMap(),
    val message: Message = EmptyMessage,
    val rootFolder: RootFolder
)
val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
val rootPath = "."
fun main() {
    stubs[FileSystemStubKey::class] = FileSystemStub()
    val initialData = Data(
        stubs = stubs,
        message = LoadRootFolder(path = rootPath),///Desktop"),
              rootFolder = RootFolder("",null, path= "")
    )
    runBlocking{
        println("start")
        val flow = suspendedFlow<Data,Boolean> {
            /* TODO find better conditions */
            conditions<Data,Boolean>{
                testObject (true)
                check {b ->b}
                updateCondition { data -> data.message != Stop}
            }
            flow{data: Data -> when(data.message) {

                is DriveStub<*> -> Parallel{
                    println("@flow.driveStub: ")
                    val response = (data.message.stub as Stub<Message>).stub(data.message.initialData as Message).get()
                    data.copy(message = response)
                }

                is FxMessage -> when(data.message){
                    is FxLaunch -> {
                        println("launch")
                        val stub = GlobalScope.async { launchFxAppFlow(appConf).evolve(InitAppStub()).get() }.await()
                        data.stubs[AppStubKey::class] = stub
                        Immediate{data.copy(message = FxLaunchResponse)}
                    }
                    is FxResponseMessage -> when(data.message){
                        is FxLaunchResponse -> Parallel{
                            println("launched")
                            // register cofigurations
                            data.copy(
                                message = DriveStub(
                                    stub = data.stubs.waitForValueToBeSet(AppStubKey::class).get(),
                                    initialData = RegisterConfigurations(
                                        arrayListOf(
                                            fxEntry {
                                                key = MainStageKey::class
                                                config = mainStageConf
                                            },
                                            fxEntry {
                                                key = MainSceneKey::class
                                                config = mainSceneConf
                                            },
                                            fxEntry {
                                                key = FolderViewKey::class
                                                config = folderViewConf
                                            },
                                            fxEntry {
                                                key = AddFileDialogKey::class
                                                config = addFileDialogConf
                                            }
                                        )
                                    )
                                )
                            )
                        }
                        is FxShowStageResponse<*,*> -> when(data.message.key){
                            is MainStageKey ->Immediate{
                                println("@flow.tralala")
                                data.stubs[MainStageStubKey::class] = data.message.stub
                                val mainSceneStub = data.message.stub.stubs()[MainSceneStubKey::class]!!
                                mainSceneStub.stubs()[FileSystemStubKey::class] = data.stubs[FileSystemStubKey::class]!!
                                data.stubs[MainSceneStubKey::class] = mainSceneStub
                                data.copy(message = DriveStub(
                                    stub = data.stubs.waitForValueToBeSet(MainSceneStubKey::class).get(),
                                    initialData = LoadedRootFolder(folder = data.rootFolder)
                                ))
                            }
                            else -> Immediate{data.copy(message = NotSupported(data.message))}
                        }
                        else -> Immediate{data.copy(message = NotSupported(data.message))}
                    }
                    else -> Immediate{data.copy(message = NotSupported(data.message))}
                }
                is RegisteredConfigurations -> Immediate{
                    data.copy(message = DriveStub(
                        stub = data.stubs.waitForValueToBeSet(AppStubKey::class).get(),
                        initialData = FxShowStage(key = MainStageKey())
                    ))
                }
                is FileSystemMessage -> when (data.message) {
                    is FileSystemRequest -> {
                        val stub = data.stubs[FileSystemStubKey::class] as Stub<FileSystemMessage>
                        when (data.message) {

                            is LoadRootFolder -> Parallel {
                                data.copy(
                                    message = stub.stub(
                                        data.message
                                    ).get() as Message
                                )
                            }
                            is LoadFolder -> Parallel {
                                data.copy(
                                    message = stub.stub(
                                        data.message
                                    ).get() as Message
                                )
                            }
                            is CreateFile -> Parallel {
                                data.copy(
                                    message = stub.stub(
                                        data.message
                                    ).get() as Message
                                )
                            }
                            is CreateFolder -> Parallel {
                                data.copy(
                                    message = stub.stub(
                                        data.message
                                    ).get() as Message
                                )
                            }
                        }
                    }
                    is FileSystemResponse -> when(data.message) {
                        /* TODO React on message */
                        is LoadedFolder -> Immediate{
                            val folder = data.message.folder.name
                            //println(folder)
                            data.copy(message = Stop)
                        }
                        is LoadedRootFolder -> Immediate{

                            data.copy(
                                rootFolder = data.message.folder,
                                message = FxLaunch
                            )
                        }
                        is CreatedFileResponse -> Immediate{ data.copy( message = Stop )}
                        is CreatedFolderResponse -> Immediate{ data.copy( message = Stop )}
                    }
                }


                is NotSupported -> Immediate{data.copy(message = Stop)}
                else -> Immediate{data.copy(message = NotSupported( receivedMessage = data.message)) }
            }}
        }
        val end = flow.evolve(initialData).get()
        //println("resulting data = "+end.get())

        //delay(10_000)
    }
    System.exit(0)
}