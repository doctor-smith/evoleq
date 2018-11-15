package org.drx.evoleq.experimental

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.drx.evoleq.parallel

fun <T> code( block:()->T ): T = block()
fun <S,T> configCode(s:S, block:(S)->T ): T = code{block(s)}

val a: Int = code<Int>{
    val v =
    code<Long>{
        4
    }
    v as Int
}
val b = configCode("ddd"){
    listOf(it.length)
}
