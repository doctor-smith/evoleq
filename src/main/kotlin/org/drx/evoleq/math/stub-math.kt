package org.drx.evoleq.math

import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.data.*
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.flow.Evolver



operator fun <D,E> Evolver<D>.plus(other: Evolver<E>): Evolver<Sum<D,E>> = object: Evolver<Sum<D,E>> {

    override suspend fun evolve(d: Sum<D, E>): Evolving<Sum<D, E>> =when(d) {
        is Sum.First ->  this@plus.evolve(d.value) map suspended{ v: D -> Sum.First<D,E>(v) }
        is Sum.Second ->  other.evolve(d.value) map suspended{ v: E -> Sum.Second<D,E>(v) }
    }

}

fun <D,E,F> sum(first: Evolver<D>, second: Evolver<E>, third: Evolver<F>): Evolver<TripleSum<D,E,F>> = object: Evolver<TripleSum<D,E,F>> {
    override suspend fun evolve(d: TripleSum<D, E, F>): Evolving<TripleSum<D, E, F>> = when(d){
        is TripleSum.First -> first.evolve(d.value) map suspended { v -> TripleSum.First(v) }
        is TripleSum.Second -> second.evolve(d.value) map suspended { v -> TripleSum.Second(v) }
        is TripleSum.Third -> third.evolve(d.value) map suspended { v -> TripleSum.Third(v) }
    }
}

fun <D,E,F,G> sum(first: Evolver<D>, second: Evolver<E>, third: Evolver<F>, fourth: Evolver<G>): Evolver<QuadSum<D, E, F, G>> = object: Evolver<QuadSum<D,E,F,G>> {
    override suspend fun evolve(d: QuadSum<D, E, F, G>): Evolving<QuadSum<D, E, F, G>> = when(d){
        is QuadSum.First -> first.evolve(d.value) map suspended { v -> QuadSum.First(v) }
        is QuadSum.Second -> second.evolve(d.value) map suspended { v -> QuadSum.Second(v) }
        is QuadSum.Third -> third.evolve(d.value) map suspended { v -> QuadSum.Third(v) }
        is QuadSum.Fourth -> fourth.evolve(d.value) map suspended { v -> QuadSum.Fourth(v) }
    }
}

fun <D,E,F,G,H> sum(first: Evolver<D>, second: Evolver<E>, third: Evolver<F>, fourth: Evolver<G>, fith: Evolver<H>): Evolver<QuintSum<D, E, F, G, H>> = object: Evolver<QuintSum<D,E,F,G,H>> {
    override suspend fun evolve(d: QuintSum<D, E, F, G,H>): Evolving<QuintSum<D, E, F, G,H>> = when(d){
        is QuintSum.First -> first.evolve(d.value) map suspended { v -> QuintSum.First(v) }
        is QuintSum.Second -> second.evolve(d.value) map suspended { v -> QuintSum.Second(v) }
        is QuintSum.Third -> third.evolve(d.value) map suspended { v -> QuintSum.Third(v) }
        is QuintSum.Fourth -> fourth.evolve(d.value) map suspended { v -> QuintSum.Fourth(v) }
        is QuintSum.Fith -> fith.evolve(d.value) map suspended { v -> QuintSum.Fith(v) }
    }
}


fun <D,E,F> Evolver<Sum<Sum<D,E>,F>>.flatten(): Evolver<TripleSum<D,E,F>> = object: Evolver<TripleSum<D,E,F>> {
    override suspend fun evolve(d: TripleSum<D, E, F>): Evolving<TripleSum<D, E, F>> = when(d) {
        is TripleSum.First -> this@flatten.evolve(Sum.First(Sum.First(d.value)))  map suspended{ v -> v.flatten() }
        is TripleSum.Second -> this@flatten.evolve(Sum.First(Sum.Second(d.value)))  map suspended{ v -> v.flatten() }
        is TripleSum.Third -> this@flatten.evolve(Sum.Second(d.value))  map suspended{ v -> v.flatten() }
    }
}


