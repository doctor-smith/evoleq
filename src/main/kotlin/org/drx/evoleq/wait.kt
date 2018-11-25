package org.drx.evoleq

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import kotlinx.coroutines.*

interface WaitFor<D> {
    suspend fun toChange(): Evolving<D>
}

class WaitForProperty<D>(
    private val property: SimpleObjectProperty<D>,
    private val delay: Long = 1L
) : WaitFor<D> {
    override suspend fun toChange(): Evolving<D> = Parallel {
        var changed = false
        val listener = ChangeListener<D> {_,_,_ -> changed = true }
        property.addListener(listener)
        while( !changed ){
            delay( delay )
        }
        property.removeListener(listener)
        property.value
    }
}