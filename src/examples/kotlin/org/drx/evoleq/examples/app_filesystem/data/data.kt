package org.drx.evoleq.examples.app_filesystem.data

import org.drx.evoleq.util.tail

sealed class FileModel(val parent: FolderModel?= null)
data class File(
    val name: String,
    val parentFolder: FolderModel?
) : FileModel(parentFolder)

sealed class FolderModel(parentF: FolderModel? = null) : FileModel(parentF)
data class Folder(
    val name: String,
    val parentFolder: FolderModel?,
    val children: ArrayList<FileModel> = arrayListOf()
) : FolderModel(parentFolder)
data class RootFolder(
    val name: String,
    val parentFolder: FolderModel?,
    val children: ArrayList<FileModel> = arrayListOf()
) : FolderModel(parentFolder)


fun Folder.add(child: FileModel): Folder = when(child){
    is File -> {
        this.children.add(child.copy(parentFolder = this))
        this
    }
    is Folder -> {
        this.children.add(child.copy(parentFolder = this))
        this
    }
    is RootFolder -> {
        this.children.add(Folder(
            name = child.name,
            parentFolder = this,
            children = child.children
        ))
        this
    }
}

fun Folder.addAll(vararg children: FileModel): Folder = this.addAll(arrayListOf(*children))

tailrec fun Folder.addAll(children: ArrayList<FileModel>): Folder = when(children.isEmpty()){
    true -> this
    false -> this.add(children.first()).addAll(children.tail())
}

fun Folder.remove(child: FileModel) = this.children.remove(child)

fun RootFolder.add(child: FileModel): RootFolder = when(child){
    is File -> {
        this.children.add(child.copy(parentFolder = this))
        this
    }
    is Folder -> {
        this.children.add(child.copy(parentFolder = this))
        this
    }
    is RootFolder -> {
        this.children.add(Folder(
            name = child.name,
            parentFolder = this,
            children = child.children
        ))
        this
    }
}

fun RootFolder.addAll(vararg children: FileModel): RootFolder = this.addAll(arrayListOf(*children))

tailrec fun RootFolder.addAll(children: ArrayList<FileModel>): RootFolder = when(children.isEmpty()){
    true -> this
    false -> this.add(children.first()).addAll(children.tail())
}

fun RootFolder.remove(child: FileModel) = this.children.remove(child)


