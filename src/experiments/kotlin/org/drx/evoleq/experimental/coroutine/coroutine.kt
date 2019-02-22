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
package org.drx.evoleq.experimental.coroutine

import kotlinx.coroutines.*
import org.drx.evoleq.evolving.Parallel

fun main() = runBlocking{
    var newJob: Job? = null
    val job = launch{
        Parallel<Unit>(scope = this){
            newJob = launch { delay(1_000) }
        }
    }
    while(newJob == null){
        delay(1)
    }

    println("job.children contains newJob: " + job.children.contains(newJob!!))
    //delay(20)
    job.cancelAndJoin()
    println("newJob is cancelled: "+newJob!!.isCancelled)
    var parallel: Parallel<Int>? = null
    var job2 : Job? = null
    val job1 = launch {
        parallel = Parallel(scope = this) {
            job2 = scope.launch {
                delay(2_000)
            }
            delay(1_000)
            1
        }
    }
    launch {
        while(parallel == null || job2 == null) {
            delay(1)
        }
        println("job1.children contains job2: " + job1.children.contains(job2!!))
        println("job1.children contains parallel.job: " + job1.children.contains(parallel!!.job()))
        println("parallel.job().children contains job2: " + parallel!!.job().children.contains(job2!!))
        println("parallel.job.children.count: " + parallel!!.job().children.count())
        parallel!!.cancel(2)
        println(parallel!!.get())
        //delay(200)
        //println(job2!!.isCancelled)
    }
    println("jfdksal")

}