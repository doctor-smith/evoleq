package org.drx.evoleq.experimental

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.*
import org.drx.evoleq.*


abstract class ChattyFlow<D,T,I,O>(
    val conditions: EvolutionConditions<D,T>,
    val flow: (D)->Evolving<D>,
    val initialMessage: I


) : Evolver<D>, TwoWayFlange<I,O> {

    val pipe: TwoWayPipe<I, O> by lazy { TwoWayPipe<I,O>() }
    init{
        input(initialMessage)
    }
    abstract fun chattyCheck(t:Pair<I,D>): Boolean

    abstract fun chattyUpdate(p: Pair<I,D>): O

    abstract fun waitForInput()

    override suspend fun evolve(data: D): Evolving<D> = Immediate {
        evolve(
            initialData = data,
            conditions = conditions
        ){
            data ->
                runBlocking {
                    waitForInput()
                }
                val input: I = pipe.oi().output().value
                val newData = when(chattyCheck(Pair(input,data))){
                    false -> Immediate{data}
                    true -> flow(data)
                }
                runBlocking {
                     chattyUpdate(Pair(input,newData.get()))
                }
                newData
        }
    }


    override fun input(stuff: I) {
        pipe.io().input(stuff)
    }

    override fun output(): SimpleObjectProperty<O> {
        return pipe.io().output()
    }
}



open class ChattyFlowBase<D,T,I,O> (
    private val chattyConditions: EvolutionConditions<TwoWayFlangedData<O, I, D>, TwoWayFlangedData<O, I, T>>,
    private val chattyFlow: (TwoWayFlangedData<O,I,D>)-> Evolving<TwoWayFlangedData<O, I, D>>,
    private val pipe: TwoWayPipe<I,O>
) : Flow<TwoWayFlangedData<O, I, D>, TwoWayFlangedData<O, I, T>>(chattyConditions, chattyFlow), TwoWayFlange<I,O> {
    override fun input(stuff: I) {
        pipe.io().input(stuff)
    }
    override fun output(): SimpleObjectProperty<O> {
        return pipe.io().output()
    }
/*
    override suspend fun evolve(data: TwoWayFlangedData<O, I, D>): Evolving<TwoWayFlangedData<O, I, D>> {
        return super.evolve(data)
    }
    */
}
data class TwoWayFlangedData<I,O,D>(val flange: TwoWayFlange<I,O>,val data: D)

class OneWayPipe<S>{
    private val input: SimpleObjectProperty<S> by lazy { SimpleObjectProperty<S>() }
    private val output: SimpleObjectProperty<S> by lazy { SimpleObjectProperty<S>() }
    init{
        output.bind(input)
    }
    fun input(stuff: S){
        input.value = stuff
    }
    fun output(): SimpleObjectProperty<S> = output
}
interface TwoWayFlange<I,O> {
    fun input(stuff: I)
    fun output(): SimpleObjectProperty<O>
}
interface TwoWayFlanges<I,O> {
    fun io(): TwoWayFlange<I,O>
    fun oi(): TwoWayFlange<O,I>
}
class TwoWayPipe<I,O>(
    private val io: OneWayPipe<I> = OneWayPipe(),
    private val oi: OneWayPipe<O> = OneWayPipe()
): TwoWayFlanges<I,O>{
    override fun io(): TwoWayFlange<I, O> = object: TwoWayFlange<I,O> {
        override fun input(stuff: I) = io.input(stuff)
        override fun output(): SimpleObjectProperty<O> = oi.output()
    }
    override fun oi(): TwoWayFlange<O, I> = object: TwoWayFlange<O,I> {
        override fun input(stuff: O) = oi.input(stuff)
        override fun output(): SimpleObjectProperty<I> = io.output()
    }
}




