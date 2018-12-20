package org.drx.evoleq.examples.app_filesystem.fx

import javafx.application.Platform
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import kotlinx.coroutines.*
import org.drx.evoleq.dsl.conditions
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.examples.app_filesystem.data.*
import org.drx.evoleq.examples.app_filesystem.message.*
import org.drx.evoleq.examples.app_filesystem.stubs
import org.drx.evoleq.examples.app_filesystem.stubs.FileSystemStub
import org.drx.evoleq.examples.app_filesystem.stubs.FileSystemStubKey
import org.drx.evoleq.examples.app_filesystem.stubs.path
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.fx.*
import org.drx.evoleq.examples.application.message.*
import org.drx.evoleq.gap.Spatula
import org.drx.evoleq.time.WaitForProperty
import tornadofx.action

class SetValueOfStringProperty(val property: SimpleStringProperty, val value: String) : Message()
object ValueOfStringPropertySet : Message()
class SetFolderAndFiles(val container: FolderViewContainer,val folder: FolderModel,val files: ArrayList<FileModel>) : Message()
object FolderAndFilesSet : Message()

/**
 *
 */
class MainStageKey
class MainStageStubKey
val mainStageConf = fxStageLazyConfiguration<Message,FxStageLazyConfiguration<Message>> {

    x = 100.0
    y = 100.0
    minWidth = 400.0
    minHeight = 300.0
    //isResizable = true

    title = "FileSystem Main Stage"
    stub = {message: Message -> Immediate{ message } }

    GlobalScope.launch {
        val newScene = mainSceneConf.configure().get()
        scene = newScene
        stubs[MainSceneStubKey::class] = newScene
        stubs[MainSceneStubKey::class]!!
    }
}

/**
 * MainSceneConfigufration
 */
class MainSceneKey
class MainSceneStubKey
val mainSceneConf = fxSceneLazyConfiguration<Message, FxSceneLazyConfiguration<Message>>{
    val box = VBox()
    box.children.add(Text("Files and Folders"))
    this@fxSceneLazyConfiguration.parent = box
}.fxStub {
    stub = {message: Message ->

        println("@mainSceneStub: ")
        when(message){
            is FileSystemResponse -> when (message){
                is LoadedFolder -> Immediate{ NotSupported(message) }
                is LoadedRootFolder -> {
                    var m: Message? = null
                    GlobalScope.launch {
                        val conf = folderViewConf
                        val node = conf.configure().get()

                        Platform.runLater {
                            (parent!! as VBox).children.add(node.node)
                        }
                        node.stubs()[FileSystemStubKey::class] = stubs[FileSystemStubKey::class]!!
                        //node.node.nameProperty().value= message.folder.name
                        //val name =  message.folder.name//node.stub(SetValueOfStringProperty(node.node.nameProperty(), message.folder.name)).get()

                        val folder = message.folder
                        println("folder content: size = ${folder.children.size}")
                        val flow = suspendedFlow<Message,Boolean> {
                            conditions = conditions {
                                testObject = true
                                check = {b: Boolean ->b}
                                updateCondition = {m:Message -> m != Stop}
                            }
                            flow = {
                                message -> when(message) {
                                    is Wait -> node.stub(WaitForPropertyMessage(node.node.output))
                                    is SetValueOfStringProperty-> node.stub(message)
                                    is ValueOfStringPropertySet -> {
                                        node.stub(SetFolderAndFiles(
                                            container = node.node,
                                            folder = folder,
                                            files = folder.children
                                        ))
                                    }
                                    is FolderAndFilesSet -> {
                                        println("HERE")
                                        Immediate{Wait}
                                    }
                                    else -> Immediate{Stop}
                                }
                            }
                        }
                        m = flow.evolve(SetValueOfStringProperty(node.node.nameProperty(), message.folder.name)).get()

                    }

                    while(m!= Stop){Thread.sleep(1)}

                    Immediate{
                        //delay(5_000)
                        m!!
                    }
                }
                is CreatedFileResponse -> Immediate{ Stop }
                is CreatedFolderResponse -> Immediate{ Stop }
            }
            else -> Immediate{NotSupported(message)}
        }
    }
}


/**
 * Folder view configuration
 */
class FolderViewKey
abstract class FolderViewContainer : VBox(){
    abstract fun nameProperty(): SimpleStringProperty
    val output = SimpleObjectProperty<Message>()
    val files: ArrayList<FileModel> by lazy { arrayListOf<FileModel>() }
    var folder: FolderModel? = null
}
val folderViewConf = fxNodeLazyConfiguration<Message,FolderViewContainer> {

    val nameProperty = SimpleStringProperty()

    val container = object : FolderViewContainer(){
        override fun nameProperty(): SimpleStringProperty {
            return nameProperty
        }

    }
    val topBox = HBox()
    val bottomBox = HBox()
    val spacer = Label()
    spacer.minWidth = 30.0
    spacer.minHeight = 3.0
    //spacer.text = "1"
    val children = VBox()
    bottomBox.children.addAll(
        spacer,
        children
    )
    val nameLabel = Label()
    nameLabel.minWidth = 200.0
    nameLabel.minHeight = 20.0
    nameProperty.addListener { _,_,nV  ->
        nameLabel.text = "$nV"
        println("name set: $nV")
    }
    container.nameProperty().value = "text"

    var state = 0
    val button = Button(">")
    button.action {
        if (state == 0) {
            state = 1
            button.text = "v"
            val stub =stubs[FileSystemStubKey::class]!! as FileSystemStub
            GlobalScope.launch {
            //println(container.files.size)
                container.files.forEach { file ->

                    when (file) {
                        is File -> {
                            val fileView =fileViewConf.configure().get().node
                            fileView.nameProperty().value = file.name
                            ParallelFx{children.children.add(fileView)}
                        }
                        is Folder -> {
                            val folderView = folderViewConf().configure().get().node
                            folderView.nameProperty().value = file.name
                            folderView.folder = file
                            val tmp = stub.stub(LoadFolder(file.path() , parent = file.parent!!))
                            (tmp.get() as LoadedFolder).folder.children.forEach {
                                folderView.files.add(it)
                            }

                            ParallelFx{ children.children.add(folderView) }
                            container.output.value = Wait
                        }
                    }
                }
            }
            container.children.add(bottomBox)
        } else {
            state = 0
            button.text = ">"
            children.children.clear()
            container.children.remove(bottomBox)
        }
    }
    nameLabel.setOnMouseClicked{_->
        val stub = stubs[AppStubKey::class]!! as Stub<Message>
        val fileStub = stubs[FileSystemStubKey::class]!! as Stub<FileSystemMessage>
        Parallel{
            var m = stub.stub(FxShowStage(AddFileDialogKey())).get()
            val s = (m as FxShowStageResponse<*,*>).stub as Stub<Message>
            m = s.stub(SetOutputMessage<Message>(
                SimpleObjectProperty()
            )).get()
            var name = ""
            when(m) {
                is AddFileMessage -> {
                    name = m.name
                    m = fileStub.stub(CreateFile(name,container.folder!!)).get()
                    container.files.add((m as CreatedFileResponse).file!!)
                }
                is AddFolderMessage -> {
                    name = m.name
                    m = fileStub.stub(CreateFolder(name,container.folder!!)).get()
                    container.files.add((m as CreatedFolderResponse).file!!)
                }
            }
            m = stub.stub(FxCloseStage(AddFileDialogKey())).get()
            container.output.value = Wait
        }

    }

    container.setOnMouseDragged {
        it.consume()
    }
    topBox.children.addAll(
        button,
        nameLabel
    )
    container.children.add(topBox)
    container
}.fxStub {
    val n = node!!()
    stub = pick({message: Message ->
        println("@folderViewStub")


        when(message){
            is WaitForPropertyMessage<*> -> Parallel{
                WaitForProperty(message.property).toChange().get() as Message
            }
            is SetValueOfStringProperty -> Parallel{
                message.property.value = message.value
                ValueOfStringPropertySet
            }
            is SetFolderAndFiles -> {

                message.container.files .addAll( message.files )
                message.container.folder = message.folder
                //println("SetFolderAndFiles ${n.files.size}")
                Immediate{FolderAndFilesSet}
            }
            is FileSystemRequest ->{
                Immediate{FxStop}//message}
            }
            is FileSystemResponse -> {
                when(message) {

                    is LoadedRootFolder -> {
                        n.nameProperty().set(message.folder.name)
                        Immediate{Stop}
                    }
                    is LoadedFolder -> Immediate{
                        //nameProperty.value = message.folder.name
                        FxStop
                    }
                    is CreatedFileResponse -> Immediate{ Stop}
                    is CreatedFolderResponse -> Immediate{ Stop}
                }
            }
            else-> Immediate{Stop}
        }
    })

}
fun folderViewConf(): LazyFxNodeConfiguration<Message,FolderViewContainer> = folderViewConf
class FileViewKey
abstract class FileViewContainer : HBox() {
    abstract fun nameProperty(): SimpleStringProperty
}
val fileViewConf = fxNodeLazyConfiguration<Message,FileViewContainer> {
    val nameProperty = SimpleStringProperty()
    val  node = object : FileViewContainer() {
        override fun nameProperty(): SimpleStringProperty = nameProperty
    }
    val nameLabel = Label("no name")
    nameLabel.textProperty().bind(nameProperty)
    node.children.add(nameLabel)

/*    val button = Button("<")
    node.children.add(button)
*/
    node
}


class AddFileDialogKey
val  addFileDialogConf = fxStageLazyConfiguration<Message, FxStageLazyConfiguration<Message>> {

    var output: SimpleObjectProperty<Message>? = null

    val container = VBox()
    scene = Scene(container, 300.0, 200.0)
    val nameLabel = Label("name")
    val nameTextField = TextField()
    val addAsFile = CheckBox("Add as file")
    val ok = Button("Ok")

    ok.action{
        output!!.value = when(addAsFile.isSelected){
            true -> AddFileMessage(nameTextField.text)
            false -> AddFolderMessage(nameTextField.text)
        }
    }

    container.children.addAll(
        addAsFile,
        HBox(nameLabel, nameTextField ),
        ok
    )

    stub = {message:Message -> when(message) {
        is SetOutputMessage<*> -> {
            output = message.output as SimpleObjectProperty<Message>
            Parallel{WaitForProperty(output!!).toChange().get()}
        }
        else-> Immediate{ Stop }

    }}
}
class SetOutputMessage<D>(val output: SimpleObjectProperty<D>): Message()
object OutputMessage : Message()
class AddFileMessage(val name: String) : Message()
class AddFolderMessage(val name: String) : Message()