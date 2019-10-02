package org.drx.evoleq.coroutines

import kotlinx.coroutines.CoroutineScope


fun <S, T> suspendOnScope(f:(S)->T): suspend CoroutineScope.(S)->T = {
        s -> f(s)
}

fun <S1,S2, T> suspendOnScope(f:(S1,S2)->T): suspend CoroutineScope.(S1, S2)->T = {
        s1,s2 -> f(s1,s2)
}

fun <S1,S2,S3, T> suspendOnScope(f:(S1,S2,S3)->T): suspend CoroutineScope.(S1, S2, S3)->T = {
        s1,s2,s3 -> f(s1,s2,s3)
}

fun <S1,S2,S3,S4, T> suspendOnScope(f:(S1,S2,S3,S4)->T): suspend CoroutineScope.(S1, S2, S3, S4)->T = {
        s1,s2,s3, s4 -> f(s1,s2,s3,s4)
}

fun <S1,S2,S3,S4,S5, T> suspendOnScope(f:(S1,S2,S3,S4,S5)->T): suspend CoroutineScope.(S1, S2, S3, S4, S5)->T = {
        s1,s2,s3,s4,s5 -> f(s1,s2,s3,s4,s5)
}