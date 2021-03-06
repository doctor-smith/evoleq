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
package org.drx.evoleq.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.gap
import org.drx.evoleq.dsl.stub
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.junit.Test
import kotlin.system.measureTimeMillis

class EvolverTest {

    @Test fun forkParallel() = runBlocking {

        val evolver = object : Evolver<Int> {
            override val scope: CoroutineScope
                get() = GlobalScope
            override suspend fun evolve(d: Int): Evolving<Int> = Parallel {
                kotlinx.coroutines.delay(200)
                d
            }
        }
        val other =object : Evolver<Int> {
            override val scope: CoroutineScope
                get() = GlobalScope
            override suspend fun evolve(d: Int): Evolving<Int> = Parallel {
                kotlinx.coroutines.delay(100)
                d
            }
        }
        val time = measureTimeMillis {
             evolver.forkParallel(other).evolve(Pair(0, 0)).get()
        }
        println(time)
        assert(time < 300)

    }

    @Test fun collectErrors() = runBlocking {

        class ErrorStack<D>(val data: D,val  exception: Exception? = null)
        val evolver = object: Evolver<Int> {
            val s = CoroutineScope(Job())
            override val scope: CoroutineScope
                get() = s
            override suspend fun evolve(d: Int): Evolving<Int> {
                if(d < 0) {
                    throw Exception("Exception")
                }
                return Immediate{d+1}
            }
        }
        fun Evolver<Int>.catchErrors() : Evolver<ErrorStack<Int>> = object: Evolver<ErrorStack<Int>> {
            override val scope: CoroutineScope
                get() = this@catchErrors.scope
            override suspend fun evolve(d: ErrorStack<Int>): Evolving<ErrorStack<Int>> =
                try{
                    Parallel{ErrorStack(this@catchErrors.evolve(d.data).get(),null)}
                } catch(exception: Exception){
                    Immediate{ErrorStack(d.data, exception)}
                }
            }


        val res1 = evolver.catchErrors().evolve(ErrorStack(1)).get()
        assert(res1.data == 2)
        assert(res1.exception == null)
        try {
            val res2 = evolver.catchErrors().evolve(ErrorStack(-1)).get()
            assert(res2.exception!!.message == "Exception")
            assert(res2.data == -1)
        } catch(e: Exception) {}
    }

    @Test fun intercept() = runBlocking {

        var interceptorCalled = false

        class Interceptor
        val interceptor = stub<String>{
            id(Interceptor::class)

            evolve{
                s -> Immediate{
                interceptorCalled = true
                    s+s
                }
            }
        }

        val stub = stub<Int> {
            evolve{ x -> when(x) {
                0 -> x.intercept(interceptor, gap{
                    from  { x -> Immediate{x.toString()} }
                    to { x, s -> Immediate{x + Integer.parseInt(s)} }
                })
                5 -> Immediate{0}
                else -> Immediate{x+1}
            } }
        }

        val res = stub.evolve(0).get()

        assert(interceptorCalled)

    }

}