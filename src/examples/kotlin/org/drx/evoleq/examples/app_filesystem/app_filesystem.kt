package org.drx.evoleq.examples.app_filesystem

import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.examples.app_filesystem.message.*
import org.drx.evoleq.examples.app_filesystem.stubs.FileSystemStub
import org.drx.evoleq.examples.app_filesystem.stubs.FileSystemStubKey
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.message.EmptyMessage
import org.drx.evoleq.examples.application.message.Message
import org.drx.evoleq.examples.application.message.NotSupported
import kotlin.reflect.KClass

data class Data(
    val stubs: HashMap<KClass<*>, Stub<*>>  = HashMap(),
    val message: Message = EmptyMessage
)
val stubs: HashMap<KClass<*>, Stub<*>> by lazy{ HashMap<KClass<*>, Stub<*>>() }

fun main() {
    stubs[FileSystemStubKey::class] = FileSystemStub()
    val initialData = Data(
        stubs = stubs
    )
    runBlocking{
        val flow = suspendedFlow<Data,Boolean> {
            /* TODO find better conditions */
            conditions = once()
            flow = {data: Data -> when(data.message) {
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
                        }
                    }
                    is FileSystemResponse -> when(data.message) {
                        /* TODO React on message */
                        is LoadedFolder, is LoadedRootFolder -> Immediate{data}
                    }
                }
                else -> Immediate{data.copy(message = NotSupported( receivedMessage = data.message)) }
            }}
        }
        val end = flow.evolve(initialData)
    }
    System.exit(0)
}