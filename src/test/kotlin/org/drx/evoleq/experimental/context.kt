package org.drx.evoleq.experimental

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Thread.sleep

abstract class Context<S,T>() {

    //abstract fun capture(f: (S)->T)
    abstract fun run(): T
}

class ParallelContext<S,T>(val s:S, val f:(S)->T) : Context<S,T>() {
    private val property: SimpleObjectProperty<T> = SimpleObjectProperty()
    private var updated = false

    init {
        val listener = ChangeListener<T>{_, oV, nV ->
            if (nV != oV) {
                updated = true
            }
        }
        property.addListener( listener )
        GlobalScope.launch {
            coroutineScope {
                launch {
                    property.value = f(s)
                    property.removeListener( listener )
                }
            }
        }

    }

    override fun run(): T {
        while(!updated){
            sleep(1)
        }
        return property.value
    }

}