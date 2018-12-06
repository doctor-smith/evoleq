package org.drx.evoleq

fun <T> ArrayList<T>.tail(): ArrayList<T> {
    val N = size

    if(N <= 1){
        return arrayListOf()
    }
    val tail = arrayListOf<T>()
    IntRange(1,N-1).forEach { tail.add(this[it]) }
    return tail
}


fun <R,S,T> ((R)->S).then(f:(S)->T): (R)->T = {r -> f(this(r))}
fun <R,S,T> ((S)->T).after(f:(R)->S): (R)->T = {r -> this(f(r))}