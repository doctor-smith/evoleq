package org.drx.evoleq.evolving

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*
import org.drx.evoleq.util.tail


interface Evolving<out D> {
    suspend fun get() : D
}


/**
 * Implementations
 * ===============
 */
/**
 * Evolution type parallel:
 * evolve async and parallel
 * - without blocking the current thread
 */
class Parallel<D>(private val delay: Long = 1, private val block: suspend () -> D ) :
    Evolving<D> {

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
