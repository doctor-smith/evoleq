package org.drx.evoleq.examples.app_filesystem.stubs

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.app_filesystem.data.Folder
import org.drx.evoleq.examples.app_filesystem.data.RootFolder
import org.drx.evoleq.examples.app_filesystem.data.addAll
import org.drx.evoleq.examples.app_filesystem.message.*
import org.drx.evoleq.examples.application.Stub
import java.io.File

class FileSystemStubKey

class FileSystemStub : Stub<FileSystemMessage> {
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
                    parentFolder = null
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


        }
        is FileSystemResponse -> Immediate{message}
    }
}