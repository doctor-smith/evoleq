/**
 * Copyright (c) 2018-2019 Dr. Florian Schmidt
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drx.evoleq.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.drx.evoleq.coroutines.BaseReceiver
import org.drx.evoleq.coroutines.Receiver
import org.drx.evoleq.coroutines.onNext
import org.drx.evoleq.dsl.*
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.stub.toFlow

interface BackgroundFlow<I,O, D,T> {
    val bgFlow: SuspendedFlow<D,T>
    val bgInput: Receiver<I>
    val bgOutput: Receiver<O>
}

abstract class SimpleLiveCycleFlow<I,D> : BackgroundFlow<I, Process.Output<D>, Process.Phase<D>, Boolean>{

    private val inputStack = arrayListOf<I>()

    val outputStack = arrayListOf<Process.Output<D>>()

    private val input: BaseReceiver<I> = CoroutineScope(Job()).receiver<I> {}
        .onNext(CoroutineScope(Job())){input : I -> inputStack.add(input)}
    private val output: BaseReceiver<Process.Output<D>> = CoroutineScope(Job()).receiver<Process.Output<D>> {}
        .onNext(CoroutineScope(Job())){ phase: Process.Output<D> ->
            outputStack.add(phase)
        }

    override val bgFlow: SuspendedFlow<Process.Phase<D>, Boolean>
        get() = stub<Process.Phase<D>>{
            id(SimpleLiveCycleFlow::class)
            evolve { phase ->
                when (phase) {
                    is Process.Phase.StartUp -> when(phase){
                        is Process.Phase.StartUp.Start -> scope.parallel{
                            with(phase) {
                                try{
                                    output.send(Process.Output.Starting( data ))
                                    Process.Phase.Runtime.Wait( onStart( data) )
                                } catch (throwable : Throwable) {
                                    output.send( Process.Output.Error( data, throwable) )
                                    Process.Phase.StartUp.Stop( data )
                                }
                            }
                        }
                        is Process.Phase.StartUp.Stop -> scope.parallel {
                            Process.Phase.Termination.Stop( phase.data )
                        }
                    }
                    is Process.Phase.Runtime -> when(phase){
                        is Process.Phase.Runtime.Wait -> scope.parallel{
                            with(phase) {
                                if( inputStack.isEmpty() ) {
                                    //println("empty input stack")
                                    output.send( Process.Output.Waiting( data ) )
                                }
                                inputStack.onNext { input ->
                                    try {
                                         onInput( input )
                                    } catch (throwable: Throwable) {
                                        output.send(Process.Output.Error( data, throwable) )
                                        Process.Phase.Runtime.Wait( data )
                                    }
                                }
                            }
                        }
                        is Process.Phase.Runtime.Stop -> scope.parallel {
                            with( phase ) {
                                try {
                                    Process.Phase.Termination.Stop( onStop( data ) )
                                } catch ( throwable : Throwable ) {
                                    output.send( Process.Output.StoppedWithError( data, throwable ) )
                                    Process.Phase.Termination.Stop( data )
                                }
                            }
                        }
                    }
                    is Process.Phase.Termination -> when(phase){
                        is Process.Phase.Termination.Stop -> scope.parallel{
                            with( phase ) {
                                output.send(Process.Output.Stopped( data ))
                                Process.Phase.Termination.Dead( data )
                            }
                        }
                        is Process.Phase.Termination.Dead -> scope.parallel{
                            phase
                        }
                    }
                }
            }
        }.toFlow<Process.Phase<D>,Boolean>(
            conditions<Process.Phase<D>,Boolean> {
                testObject( true )
                check{ b -> b }
                updateCondition{ phase -> phase !is Process.Phase.Termination.Dead }
            }
        )
    init{
        Parallel{
            val phase = bgFlow.evolve(Process.Phase.StartUp.Start(initData())).get()
            assert(phase is Process.Phase.Termination.Dead )
        }
    }




    override val bgInput: Receiver<I>
        get() = input

    override val bgOutput: Receiver<Process.Output<D>>
        get() = output

    abstract fun initData(): D

    abstract fun onStart(data: D): D

    abstract fun onInput(input: I): Process.Phase.Runtime<D>

    abstract fun onStop(data: D): D
}

