package org.drx.evoleq.examples.propabilistic

import kotlin.math.*
import kotlin.random.Random


/**
 * Returns a (mu, sigma^2)-distributed gauss-distribution
 */
tailrec fun gaussPolar(mu: Double, sigma: Double) : Double {
    val x = Random.nextDouble(from = -1.0, until = 1.0)
    val y = Random.nextDouble(from = -1.0, until = 1.0)

    val q = x*x +y*y

    if(q != 1.0 && q != 0.0){
        val p = sqrt(-2*ln(q)/q)
        return mu + abs( sigma ) * x*p
    }
    return gaussPolar(mu, sigma)
}


