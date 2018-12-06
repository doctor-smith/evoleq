package org.drx.evoleq.playground

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CoroutinePlayground {
    @Test
    fun exit() = runBlocking{
        val x = {

            var z = 0
            GlobalScope.launch exit@{
                z = 5
                return@exit
            }
            println(z)
        }
    }
    @Test
    fun exitFun() {
        exit@ fun<T,D> T.exit(block:T.()->D): D = this.block()
        var y = exit@{
            val N = 3.0
            return@exit N
            Unit


        }
    }
}