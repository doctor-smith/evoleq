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
package org.drx.evoleq.dsl

import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.evolving.Parallel
import org.drx.evoleq.stub.Keys
import org.drx.evoleq.stub.Stub
import org.drx.evoleq.time.TimeoutKey

/**
 *
 */
open class RacingStubConfiguration<D,P>() : ObservingStubConfiguration<D,P?>() {

    constructor(scope: CoroutineScope) : this(){
        this.scope = scope
    }


    private val property = SimpleObjectProperty<P?>()
    private var cnt = 0
    private var numberOfNullResponses = 0
    private var timeout: Long  = Long.MAX_VALUE
    private val cancellables: ArrayList<Parallel<Evolving<P?>>> by lazy { ArrayList<Parallel<Evolving<P?>>>() }
    /**
     * Add a new driver to the race
     */
    fun driver(stub: Stub<P?>) {
        child(
            Keys[cnt]!!,
            stub<P?>{
                evolve{p: P? ->
                    val result = stub.evolve(p).get()
                    if(result != null) {
                        property.value = result
                    } else {
                        numberOfNullResponses++
                        if(numberOfNullResponses == cnt){
                            property.value = result
                        }
                    }
                    Immediate{result}
                }
            }
        )
        cnt++
    }

    /**
     * Add a new driver to the race
     */
    fun driver(stub: suspend (P?)-> Evolving<P?>) {
        val driver = stub<P?>{ evolve(stub) }
        driver(driver)
    }

    /**
     * Set a timeout
     */
    fun timeout(millis: Long){
        timeout = millis
    }

    override fun configure(): Stub<D> {
        var setup = true
        scope.launch { async{
            observe(property)
            property.addListener{_,_,nV -> cancellables.forEach{it.cancel(Immediate{null})}}
            child(
                TimeoutKey::class,
                stub<Unit> {
                    evolve {
                        parallel {
                            delay(timeout)
                            property.value = null
                        }
                    }
                }
            )
            evolve { data ->
                parallel {
                    IntRange(0, cnt - 1).forEach {
                        cancellables.add( Parallel { (child(Keys[it]!!) as Stub<P?>).evolve(null) })
                    }
                    parallel<Unit> { (child(TimeoutKey::class) as Stub<Unit>).evolve(Unit) }
                    data
                }
            }}.await()
            setup = false
        }
        while(setup){
            Thread.sleep(0,1)
        }
        return super.configure()
    }
}

/**
 * Configure a race between certain stubs
 * Important: You need to configure a gap for the underlying observing flow
 */
fun <D,P> racingStub(configuration: RacingStubConfiguration<D,P>.()->Unit) = configure(configuration)
fun <D,P> CoroutineScope.racingStub(configuration: RacingStubConfiguration<D,P>.()->Unit) = configure(this,configuration)
