package org.drx.evoleq.math

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.util.tail


/**
 * Functoriality:
 * ==============
 */
suspend infix
fun <D1,D2> Evolving<D1>.map(f:  (D1) -> D2) : Evolving<D2> = object : Evolving<D2> {
    override suspend fun get(): D2 = f ( this@map.get() )
}
suspend infix
fun<D1,D2> Evolving<D1>.lift(f:  (D1) -> D2) : (Evolving<D1>) -> Evolving<D2> = { ev -> Immediate { (ev map f).get() } }
/**
 * Monad
 * =====
 */
/**
 * Enter the monad
 */
fun <D> etaEvolving(data: D): Evolving<D> = Immediate { data }

/**
 * Multiply evolvings
 */
suspend fun <D> muEvolving(evolving: Evolving<Evolving<D>>): Evolving<D> {
    return evolving.get()
}

/**
 * Fish operator / multiplication on kleisli arrows
 */
suspend operator
fun <R,S,T> ( (R)-> Evolving<S>).times(flow: (S)-> Evolving<T>) : (R)-> Evolving<T> = {
        r ->
    Immediate { muEvolving(this(r) map flow).get() }
}
suspend fun <S,T> process(first:(S)-> Evolving<T>, vararg steps: (T)-> Evolving<T>): (S)-> Evolving<T> =
    when(steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = arrayListOf(*steps).tail()
            process(first * next, tail)
        }
    }
tailrec suspend fun <S,T> process(first: (S)-> Evolving<T>, steps: ArrayList<(T)-> Evolving<T>>): (S)-> Evolving<T> =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            process(first * next, tail)
        }
    }

/**
 * Comonad
 * =======
 */
fun <D> deltaEvolving(ev: Evolving<D>): Evolving<Evolving<D>> =
    etaEvolving(ev)
/**
 * Bird operator on cokleisli arrows
 */
suspend operator
fun <R, S, T> ((Evolving<R>)->S).div(f:(Evolving<S>)->T): (Evolving<R>)->T = {
        evR -> f(etaEvolving(this(evR)))
}

//suspend fun <S,T> coklEvolving(f:(S)->T): (Evolving<S>)->T = {evolving -> f(evolving.get()) }
