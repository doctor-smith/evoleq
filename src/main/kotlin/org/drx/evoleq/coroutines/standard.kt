package org.drx.evoleq.coroutines

/**
 * Explicitly unsuspended function
 * Usage: If you define a function as a lambda {s:S->t:T}, then the compiler dosn't know if the lambda is suspended or not
 */
fun <S, T> unSuspended(lambda :(S)->T): (S)->T = lambda
fun <S, T> standard(lambda:(S)->T): (S)->T = lambda

fun <S1, S2 ,T> standard(lambda: (S1,S2)->T): (S1,S2)->T = lambda
fun <S1, S2 ,S3, T> standard(lambda: (S1,S2,S3)->T): (S1,S2,S3)->T = lambda
fun <S1, S2 ,S3, S4, T> standard(lambda: (S1,S2,S3,S4)->T): (S1,S2,S3,S4)->T = lambda