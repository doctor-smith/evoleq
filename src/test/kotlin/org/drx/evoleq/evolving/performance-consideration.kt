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
package org.drx.evoleq.evolving

import kotlinx.coroutines.*
import org.drx.evoleq.coroutines.blockUntil
import org.drx.evoleq.dsl.parallel
import org.drx.evoleq.util.booleanProperty

class PerformanceConsideration {

    //@Test
    fun compare() = runBlocking {

        val number = 500

        var count:Int = 0
        val f: suspend ()->Unit = {
        while(count < number){
            delay(1)
            count++
        }}

        val time1Deferred = report("delay in while", number, f)
        val property = booleanProperty(false)
        parallel {
            delay( number.toLong())
            property.value = true
        }
        val time2Deferred = report("block until", number){

            blockUntil(property){x -> x}
        }
        val time1 = time1Deferred.await()
        val time2 = time2Deferred.await()
        println("time1 / time2 = ${time1.toDouble()/time2.toDouble()}")
        println("time1 - time2 = ${time1 -time2}\n\n")

    }

    //@Test
    fun  compare2() = runBlocking {
        val number = 1_0000;
        val results = arrayListOf<Pair<Double,Double>>()


        val delayInWhileCase: suspend ()->Unit = {
            var count:Int = 0
            while(count < number){
                delay(1)
                count++
            }}

        val blockUntilCase: suspend ()->Unit = {
            val property = booleanProperty(false)
            parallel {
                delay( number.toLong())
                property.value = true
            }
            blockUntil(property){x -> x}
        }


        IntRange(1,1000).forEach {
            parallel{
                results.add(Pair(
                    report("while",number,delayInWhileCase).await().toDouble(),
                    report("blockuntil",number,blockUntilCase).await().toDouble()
                ))
            }
            Unit
        }
        delay((1.3 * number*10).toLong())
        val r = results.map{
            pair -> Pair(pair.first / pair.second, pair.first - pair.second )
        }
        r.forEach {
            println(it)
        }

    }

    private suspend fun report(desc: String, number: Int, f: suspend ()->Unit): Deferred<Long> = coroutineScope {
        async {
            var time = System.currentTimeMillis()
            f()
            time = System.currentTimeMillis() - time


            println(desc)
            println("number: $number")
            println("absulute time: $time ms")
            println("ratio = ${time.toDouble() / number.toDouble()}\n")


            time
        }
    }
}