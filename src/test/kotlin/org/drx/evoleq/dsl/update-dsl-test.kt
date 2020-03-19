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

import kotlinx.coroutines.runBlocking
import org.drx.evoleq.stub.ID
import org.junit.Test

class UpdateDslTest {



    @Test fun simpleUpdate() = runBlocking{
        class X(val x:Int = 1)
        val f: X.()->X = {X(x+1)}
        val update = Update<ID,X>(DefaultSender::class){f()}
        val y = update.run(X())
        assert(y.data.x==2)

        val update2 = update * update
        val y1 = update2.run(X())
        assert(y1.data.x == 3)
    }

    @Test fun updateConsumer() = runBlocking{
        data class Q(val q: Int)
        data class P(val p1: Int, val q: Q)
        data class W(val w: Int, val p: P)

        class KeyQ
        class KeyP
        class KeyW

        val q: suspend Q.()->Q = {Q(q +2)}
        val qUpdate: suspend Q.()->Update<ID,Q> = {Update<ID,Q>(KeyQ::class){q()}}
        val updateQinP: suspend (P, suspend Q.()->Update<ID,Q>)->Update<ID,P> = {
            p, f -> Update(KeyP::class) p@{
                with(p.q.f().run(p.q)) {
                    if(senderId == KeyQ::class) {
                        P(p.p1, data)
                    } else {
                       this@p
                    }
                }

            }
        }
        val result = UpdateConsumer(updateQinP).update(qUpdate).run( P(1,Q(1)) )
        println(result)
        assert(UpdateConsumer(updateQinP).update(qUpdate).run( P(1,Q(1)) ).data == P(1,Q(3)))


        val pUpdate: suspend P.()->Update<ID,P> = { Update(DefaultSender::class){ P(p1+1, Q(this.q.q + 1)) } }

        val updatePinW: suspend (W, suspend P.()->Update<ID,P>)->Update<ID,W> = {
                w, f -> Update(DefaultSender::class){
                W(w.w, w.p.f().run(p).data)
            }
        }

        val nestedUpdateConsumer = UpdateConsumer(updatePinW) * UpdateConsumer(updateQinP)
        assert(nestedUpdateConsumer.update(qUpdate).run( W(0,P(0, Q(0))) ).data == W(0,P(0, Q(2))))
    }
}