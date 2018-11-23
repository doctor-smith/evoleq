package org.drx.evoleq

fun <T> ArrayList<T>.tail(): ArrayList<T> {
    val N = size

    if(N <= 1){
        //println("tail(): size = ${N-1}")
        return arrayListOf()
    }
    //println("tail(): size = ${N-1}")
    val tail = arrayListOf<T>()
    IntRange(1,N-1).forEach { tail.add(this[it]) }
    return tail
}

