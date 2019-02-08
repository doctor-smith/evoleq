/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.gap.Filling

class FillingConfiguration<P> : Configuration<Filling<P>> {

    var filler: suspend (P)-> Evolving<P> = {p->Immediate{p}}

    override fun configure(): Filling<P> {
        return object : Filling<P>{
            override val filler: suspend (P) -> Evolving<P>
                get() = this@FillingConfiguration.filler
        }
    }

    fun filler(f:suspend (P)-> Evolving<P>) {
        this.filler = f
    }
}

fun <P> filling(configuration: FillingConfiguration<P>.()->Unit): Filling<P> = configure(configuration)