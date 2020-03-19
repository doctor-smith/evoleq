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
package org.drx.evoleq.dsl



class DefaultSender

data class Updated<out Id,out D>(val senderId: Id, val data: D)
data class Update<Id,D>(val update: suspend D.()->Updated<Id,D>){
    suspend fun run(data: D): Updated<Id,D> = update(data)
}

@Suppress("FunctionName")
fun <Id,D> Update(senderId: Id, update: suspend D.()->D): Update<Id,D> = Update{ Updated(senderId, update(this)) }

/**
 * Update is a Writer; Clear: (Hom(D,D), o) is a Monoid.
 */
@EvoleqDsl
suspend operator fun<Id,D> Update<Id,D>.times(next: Update<Id,D>): Update<Id,D> = Update{
    next.run(this@times.run(this).data)
}

data class PartialUpdate<Id,W,P>(val setter: suspend W.(P)->W,val update: suspend P.()->Updated<Id,P>) {
    suspend fun run(p: P): Update<Id,W> = with(p.update()){
        Update(senderId){setter(data)}
    }
}

@EvoleqDsl
suspend fun <Id,W, P> Update<Id,P>.within(whole : suspend W.(P)->W) : suspend P.()->Update<Id,W> = { PartialUpdate(whole, update).run(this) }


@EvoleqDsl
data class UpdateConsumer<Id,W, P>(val consume: suspend W.(suspend P.()->Update<Id,P>)->Update<Id,W>){
    suspend fun update(update: suspend P.()->Update<Id,P>): Update<Id,W> = Update{
        consume(update).run(this)
    }
}

fun <A,B, T> (suspend (A,B)->T).flipArguments(): suspend (B,A)->T = {b,a -> this(a,b)}

@Suppress("FunctionName")
fun <Id,W, P> UpdateConsumer(consume: suspend ((suspend P.() -> Update<Id,P>)).(W) -> Update<Id,W>): UpdateConsumer<Id,W,P> =
    UpdateConsumer(consume.flipArguments())

@EvoleqDsl
suspend operator fun <Id,W,P,Q> UpdateConsumer<Id,W,P>.times(next: UpdateConsumer<Id,P, Q>): UpdateConsumer<Id,W, Q> {
    val c: suspend (W, suspend Q.() -> Update<Id,Q>) -> Update<Id,W> = { w, f ->
        Update { this@times.update { next.update(f) }.run(w) }
    }
    return UpdateConsumer(c)
}