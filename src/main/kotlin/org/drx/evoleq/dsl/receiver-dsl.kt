package org.drx.evoleq.dsl

import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Receiver<D>(val actor: SendChannel<D>, val channel: Channel<D>){
    suspend fun send(data: D) = actor.send(data)
}


suspend fun <D> CoroutineScope.receiver(
    context: CoroutineContext = EmptyCoroutineContext,
    capacity: Int = 0,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    onCompletion: CompletionHandler? = null,
    block: suspend ActorScope<D>.(D) -> Unit = {}
): Receiver<D> {
    val c: Channel<D> = Channel()
    val actor = actor<D>(context, capacity, start, onCompletion){
        for(d in channel){
            c.send(d)
            block(d)
        }
    }

    return Receiver(actor,c)
}
