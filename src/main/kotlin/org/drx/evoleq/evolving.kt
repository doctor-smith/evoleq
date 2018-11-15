package org.drx.evoleq

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*


interface Evolving<out D> {
    suspend fun get() : D
}

/**
 * Functoriality:
 * ====================================================================================================================
 * (D)->Evolving<D> is a functor
 */
suspend infix
fun <D1,D2> Evolving<D1>.map(f: suspend (D1) -> D2) : Evolving<D2> = object : Evolving<D2> {
    override suspend fun get(): D2 = f ( this@map.get() )
}

/**
 * Monad
 * =======================================
 */
/**
 * Enter the monad
 */
fun <D> eta_Evolving(data: D): Evolving<D> = Immediate{data}

/**
 * Multiply evolvings
 */
suspend fun <D> mu(evolving: Evolving<Evolving<D>>): Evolving<D> {
    return evolving.get()
}

/**
 * Fish operator on kleisli arrows
 */
suspend operator
fun <R,S,T> (suspend (R)->Evolving<S>).times( flow: suspend (S)->Evolving<T>) : suspend (R)->Evolving<T> = {
    r -> mu ( this( r ) map flow  )
}


/**
 * Implementations
 * =====================================
 */
/**
 * Evolution type parallel:
 * evolve async and parallel
 * - without blocking the current thread
 */
class Parallel<D>( private val block: suspend () -> D ) : Evolving<D> {

    private val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    private var updated = false
    init {
        property.addListener { _, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        GlobalScope.launch {
            coroutineScope {
                launch {
                    property.value = block()
                }
            }
        }
    }
    override suspend fun get(): D {
        while(!updated){
            delay(1)
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
