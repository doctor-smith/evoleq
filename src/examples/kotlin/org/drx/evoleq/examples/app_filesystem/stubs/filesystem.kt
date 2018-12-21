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
package org.drx.evoleq.examples.app_filesystem.stubs

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.examples.app_filesystem.data.FileModel
import org.drx.evoleq.examples.app_filesystem.data.Folder
import org.drx.evoleq.examples.app_filesystem.data.RootFolder
import org.drx.evoleq.examples.app_filesystem.data.addAll
import org.drx.evoleq.examples.app_filesystem.message.*
import org.drx.evoleq.examples.application.Stub
import java.io.File
import kotlin.reflect.KClass

class FileSystemStubKey

class FileSystemStub : Stub<FileSystemMessage> {

    val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }
    override fun stubs(): HashMap<KClass<*>, Stub<*>> = stubs

    override suspend fun stub(message: FileSystemMessage): Evolving<FileSystemMessage> = when (message) {
        is FileSystemRequest -> when (message) {
            is LoadFolder -> {
                val file = File(message.folder)
                val folder = Folder(
                    name = file.name,
                    parentFolder = message.parent
                )
                folder.addAll(*file.listFiles()
                    .map{ file -> when(file.isFile){
                            true -> org.drx.evoleq.examples.app_filesystem.data.File(file.name, null)
                            false -> Folder(file.name, null)
                    }}
                    .toTypedArray()
                )
                Immediate{LoadedFolder(folder)}
            }
            is LoadRootFolder -> {
                val file = File(message.path)
                val folder = RootFolder(
                    name = file.name,
                    parentFolder = null,
                    path = message.path
                )
                folder.addAll(*file.listFiles()
                    .map{ file -> when(file.isFile){
                        true -> org.drx.evoleq.examples.app_filesystem.data.File(file.name, null)
                        false -> Folder(file.name, null)
                    }}
                    .toTypedArray()
                )
                Immediate{LoadedRootFolder(folder)}
            }
            is CreateFile -> Parallel{
                val name = message.name
                val parent = message.parent
                val file = File(parent.path() + "/" + name)
                file.createNewFile()
                CreatedFileResponse(org.drx.evoleq.examples.app_filesystem.data.File(name,parent))
            }
            is CreateFolder -> Parallel{
                val name = message.name
                val parent = message.parent
                val file = File(parent.path() + "/" + name)
                file.mkdir()
                CreatedFolderResponse(org.drx.evoleq.examples.app_filesystem.data.Folder(name,parent))
            }

        }
        is FileSystemResponse -> Immediate{message}
    }
}


fun FileModel.path(): String = when(this){
    is RootFolder -> this.path
    is Folder -> parent!!.path() +"/"+ this@path.name
    is org.drx.evoleq.examples.app_filesystem.data.File -> parent!!.path() + this@path.name
}