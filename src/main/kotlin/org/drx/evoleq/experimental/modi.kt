package org.drx.evoleq.experimental

sealed class EvolutionMode {
    //object ASYNC_COROUTINE : EvolutionMode()
    object PARALLEL : EvolutionMode()
    object ASYNC : EvolutionMode()
    //object COROUTINE : EvolutionMode()
}