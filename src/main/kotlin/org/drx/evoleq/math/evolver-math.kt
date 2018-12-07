package org.drx.evoleq.math

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Evolver

class Conjugation<S,T>(val invert:(T)->S, val forward:(S)->T)

fun <D,E> Evolver<D>.conjugate(f: Conjugation<D,E>): Evolver<E> {
    return object: Evolver<E> {
        override suspend fun evolve(e: E): Evolving<E> {
            return this@conjugate.evolve(f.invert(e)).map (f.forward)
        }
    }
}