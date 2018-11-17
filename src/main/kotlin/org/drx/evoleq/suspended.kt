package org.drx.evoleq

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