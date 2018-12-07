package org.drx.evoleq.coroutines

/**
 * Suspend an ordinary function
 */
fun <S,T> suspended(f:(S)->T): suspend (S)->T  {
    val suspended: suspend (S)->T
    suspended = { s -> f(s)}
    return suspended
}
fun<T> T.suspended(): suspend ()->T  {
    return {this}
}

fun <S, T> suspended(vararg functions: (S)->T): SuspendedFunctions<S,T> = SuspendedFunctions(*functions.map{f -> suspended(f)}.toTypedArray())


class SuspendedFunctions<S,T>(vararg functions: suspend (S)->T) {
    val functions = functions
    fun isEmpty(): Boolean = functions.isEmpty()
    fun first(): suspend (S)->T = functions[0]

    fun tail(): SuspendedFunctions<S,T> {
        val list = arrayListOf<suspend (S)->T>()
        IntRange(1,functions.size-1).forEach { list.add(functions[it]) }
        return SuspendedFunctions(*list.toTypedArray())
    }
}