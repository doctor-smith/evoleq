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
package org.drx.evoleq.coroutines

import kotlinx.coroutines.*
import org.drx.evoleq.dsl.immediate
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.evolving.Parallel
import org.junit.Test

class CoroutinePlaygraound {

    @Test fun scopes1() = runBlocking {
        val scope = CoroutineScope(Job())
        var innerJob: Job? = null
        var innerParallel: Parallel<Unit>? = null

        val x = scope.launch {
            innerJob = launch { delay(10_000) }
            innerParallel = parallel{ delay(10_000)}

        }
        while(innerJob == null || innerParallel == null){
            delay(1)
        }
        scope.cancel()
        assert(innerJob!!.isCancelled)
        assert(innerParallel!!.job.isCancelled)
        assert(innerParallel!!.job.isCancelled)

        // the hard case
        val scope1 = CoroutineScope(Job())
        val parallels = arrayListOf<Parallel<Unit>>()
        scope1.launch {
            IntRange(1,1000).forEach {
                delay(1)
                val x = parallel{delay(10_000)}
                parallels.add(x)
            }
        }
        delay(1_000)
        println("${parallels.size} parallels")
        scope1.cancel()
        while(scope1.isActive){
            delay(1)
        }
        parallels.forEach{
            assert(it.job.isCancelled)
        }

    }

    @Test fun scopes2() = runBlocking{
        val scope1 = GlobalScope
        val parallels = arrayListOf<Parallel<Unit>>()
        val job = scope1.launch {
            IntRange(1,1000).forEach {
                delay(1)
                val x = parallel{delay(10_000)}
                parallels.add(x)
            }
        }
        delay(1_000)
        println("${parallels.size} parallels")
        job.cancel()
        while(job.isActive){
            delay(1)
        }
        parallels.forEach{
            assert(it.job.isCancelled)
        }
    }

    @Test fun immediateBlocksCurrentThread() {
        val scope = CoroutineScope(Job())
        val startTime = System.currentTimeMillis()
        val imm = scope.immediate{delay(1000)}
        val time = System.currentTimeMillis()-startTime
        assert(time>=1_000)


    }

    @Test fun superviseJob() = runBlocking {
        var sJ : Job? = null
        var cS: CoroutineScope? = null
        fun Job.superviseBy(job: Job) : Job {
            sJ = Job(job)

            cS = CoroutineScope(sJ!!)+ this@superviseBy//.launch{this@superviceBy.join()}
            //cS!!+sJ!!
            //val j = SupervisorJob(job)
            //job.children.plus(this)
            return job
        }

        val parent = Job()
        val child = Job()
        val res = child.superviseBy(parent)


        println("res == parent: ${res == parent}")
        println("parent.child contains sJ: ${parent.children.contains(sJ!!)}")
        println("count children of parent: ${parent.children.count()}")
        println("count children of sJ: ${sJ!!.children.count()}")
        println("count children of cS: ${cS!!.coroutineContext[Job]!!.children.count()}")
        parent.cancel()
        delay(1_000)
        println("parent.isCancelled = ${parent.isCancelled}")
        println("child.isCancelled = ${child.isCancelled}")
        println("sJ.isCancelled = ${sJ!!.isCancelled}")
    }


    @Test fun y() = runBlocking{
        //val x = onScope<CoroutineScope,()->Int, Evolving<Int>>{ scope, f -> scope.parallel { f() } }
        fun <D> CoroutineScope.par(f: suspend CoroutineScope.()->D): CoroutineScope.()->Parallel<D> = onScope<Parallel<D>>{ this@par.parallel { f() }}

        val res1 = par{
            delay(1_000)
            7
        }
        val s = GlobalScope.res1()
    }


    @Test fun suspendFunctionsAreExtensions() = runBlocking{
        fun  f(f:CoroutineScope.()->Unit)  {}
        fun f(f: suspend ()->Unit) {}
        fun  f(f: suspend CoroutineScope.()->Unit)  {}



    }
}