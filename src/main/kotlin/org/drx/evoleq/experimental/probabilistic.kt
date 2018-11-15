package org.drx.evoleq.experimental

import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

val x:Double = Random.nextDouble()
/**
 * Only valid for 0 < y < sqrt(2*PI)* sigma
 */
fun gaussGerm(y : Double, mu: Double, sigma: Double) : Double  {
    val signum = if(Random.nextInt() > 0) 1 else -1

    return mu + signum * sqrt( -2 * ln( sqrt( 2 * PI ) * sigma * y) )
}

fun gauss(mu: Double, sigma: Double) : Double = gaussGerm(
    Random.nextDouble(from = 0.000_000_001, until = 1.0 / (sqrt(2*PI)*sigma) ),
    mu, sigma
)