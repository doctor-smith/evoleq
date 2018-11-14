package org.drx.evoleq

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*


/**
 * Flow of the evolution equation
 */
suspend fun <D,T,C > evolve(
    data: D,
    testObject: T,
    condition:(T)->Boolean,
    updateCondition: (D)-> T,
    flow: (D)->Deferred<D>
    ): D = when ( condition( testObject ) ) {
            false -> data
            true ->  asyncCoroutine {
                val newData = flow(data).await()
                val newTestObject = updateCondition(newData)
                evolve<D, T, C>(newData, newTestObject, condition, updateCondition, flow)
            }
    }




suspend fun <D> asyncCoroutine(block: suspend ()->D): D {
    val property: SimpleObjectProperty<D> = SimpleObjectProperty()
    var updated = false
    property.addListener{ _, oV, nV ->
        if(nV != oV) {
            updated = true
        }
    }
    coroutineScope{ launch{
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

/*
   var prop = SimpleObjectProperty<D>()
   var updated = false
   prop.addListener{_,oV,nV ->
       if(nV != oV) {
           updated = true
       }
   }
   coroutineScope{ launch {
       val newData = flow(data).await()
       val newTestObject = updateCondition(newData)
       prop.value = evolve<D, T, C>(newData, newTestObject, condition, updateCondition, flow)
   }}
   GlobalScope.async {
       while(!updated){
           delay(1)
       }
       val result = prop.value
       result
   }.await()
*/