package org.drx.evoleq.examples.app_filesystem.message

import org.drx.evoleq.examples.app_filesystem.data.FileModel
import org.drx.evoleq.examples.app_filesystem.data.Folder
import org.drx.evoleq.examples.app_filesystem.data.FolderModel
import org.drx.evoleq.examples.app_filesystem.data.RootFolder
import org.drx.evoleq.examples.application.message.Message

sealed class FileSystemMessage : Message()
sealed class FileSystemRequest : FileSystemMessage()
data class LoadFolder(val folder: String, val parent: FolderModel): FileSystemRequest()
data class LoadRootFolder(val path: String): FileSystemRequest()

sealed class FileSystemResponse : FileSystemMessage()
data class LoadedFolder(val folder: Folder) : FileSystemResponse()
data class LoadedRootFolder(val folder: RootFolder): FileSystemResponse()
