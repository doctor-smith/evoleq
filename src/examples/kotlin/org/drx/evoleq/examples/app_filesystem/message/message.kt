package org.drx.evoleq.examples.app_filesystem.message

import org.drx.evoleq.examples.app_filesystem.data.*
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.message.Message

object Stop : Message()

sealed class FileSystemMessage : Message()
sealed class FileSystemRequest : FileSystemMessage()
data class LoadFolder(val folder: String, val parent: FolderModel): FileSystemRequest()
data class LoadRootFolder(val path: String): FileSystemRequest()
data class CreateFile(val name: String, val parent: FolderModel) : FileSystemRequest()
data class CreateFolder(val name: String, val parent: FolderModel) : FileSystemRequest()


sealed class FileSystemResponse : FileSystemMessage()
data class LoadedFolder(val folder: Folder) : FileSystemResponse()
data class LoadedRootFolder(val folder: RootFolder): FileSystemResponse()
data class CreatedFileResponse(var file: File?) : FileSystemResponse()
data class CreatedFolderResponse(var file: Folder?) : FileSystemResponse()

sealed class FileSystemViewStubMessage: Message()
data class ShowFolder(val folder: Folder) : FileSystemViewStubMessage()
data class ShownFolder(val folder: Folder, val stub: Stub<Message>)
