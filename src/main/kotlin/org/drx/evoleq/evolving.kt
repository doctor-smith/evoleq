package org.drx.evoleq

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*
import org.drx.evoleq.util.tail


interface Evolving<out D> {
    suspend fun get() : D
}

/**
 * Functoriality:
 * ==============
 */
suspend infix
fun <D1,D2> Evolving<D1>.map(f:  (D1) -> D2) : Evolving<D2> = object : Evolving<D2> {
    override suspend fun get(): D2 = f ( this@map.get() )
}
suspend infix
fun<D1,D2> Evolving<D1>.lift(f:  (D1) -> D2) : (Evolving<D1>) -> Evolving<D2> = {ev -> Immediate{(ev map f).get()}}
/**
 * Monad
 * =====
 */
/**
 * Enter the monad
 */
fun <D> etaEvolving(data: D): Evolving<D> = Immediate{ data }

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
fun <R,S,T> ( (R)->Evolving<S> ).times( flow: (S)->Evolving<T>) : (R)->Evolving<T> = {
    r -> Immediate{ muEvolving ( this( r ) map flow  ).get() }
}
suspend fun <S,T> process(first:(S)->Evolving<T>, vararg steps: (T)->Evolving<T>): (S)->Evolving<T> =
    when(steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = arrayListOf(*steps).tail()
            process(first * next, tail)
        }
    }
tailrec suspend fun <S,T> process(first: (S)->Evolving<T>, steps: ArrayList<(T)->Evolving<T>>): (S)->Evolving<T> =
    when (steps.isEmpty()) {
        true -> first
        false -> {
            val next = steps.first()
            val tail = steps.tail()
            process(first * next, tail )
        }
    }

/**
 * Comonad
 * =======
 */
fun <D> deltaEvolving(ev: Evolving<D>): Evolving<Evolving<D>> = etaEvolving(ev)
/**
 * Bird operator on cokleisli arrows
 */
suspend operator
fun <R, S, T> ((Evolving<R>)->S).div(f:(Evolving<S>)->T): (Evolving<R>)->T = {
    evR -> f(etaEvolving(this(evR)))
}

//suspend fun <S,T> coklEvolving(f:(S)->T): (Evolving<S>)->T = {evolving -> f(evolving.get()) }


/**
 * Implementations
 * ===============
 */
/**
 * Evolution type parallel:
 * evolve async and parallel
 * - without blocking the current thread
 */
class Parallel<D>(private val delay: Long = 1, private val block: suspend () -> D ) : Evolving<D> {

    private val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    private var updated = false
    init {
        val listener = ChangeListener<D>{_, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        property.addListener( listener )
        GlobalScope.launch {
            coroutineScope {
                launch {
                    property.value = block()
                    property.removeListener( listener )
                }
            }
        }
    }
    override suspend fun get(): D {
        while(!updated){
            delay(delay)  // reason why get has to be suspended
        }
        return property.value
    }
}
/**
 * Evolution type function
 */
suspend fun <D> parallel( block: suspend () -> D ): D {
    val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    var updated = false
    property.addListener{ _, oV, nV ->
        if(nV != oV) {
            updated = true
        }
    }
    coroutineScope{ launch {
        property.value = block()
    }}
    return GlobalScope.async {
        while(!updated){
            delay(1)
        }
        val result = property.value
        result
    }.await()
}

/**
 * Immediate: return immediately
 */
class Immediate<D>(private val block: suspend ()->D) : Evolving<D> {
    override suspend fun get(): D = block()
}
