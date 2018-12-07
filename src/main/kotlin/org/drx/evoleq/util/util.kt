package org.drx.evoleq.util

fun <T> ArrayList<T>.tail(): ArrayList<T> {
    val N = size

    if(N <= 1){
        return arrayListOf()
    }
    val tail = arrayListOf<T>()
    IntRange(1,N-1).forEach { tail.add(this[it]) }
    return tail
}

