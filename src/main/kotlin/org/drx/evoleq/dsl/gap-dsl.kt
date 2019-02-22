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

import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.gap.Gap

open class GapConfiguration<W,P> : Configuration<Gap<W,P>> {

    private var from: ((W)->Evolving<P>)? = null
    private var to: ((W)->((P)->Evolving<W>))? = null

    fun from(start: (W)->Evolving<P>) {
        from = start
    }
    fun to(end : (W)->((P)->Evolving<W>)) {
        to = end
    }
    fun to(end: (W,P)-> Evolving<W>) {
        to = {w ->  {p -> end(w,p)}}
    }

    override fun configure(): Gap<W, P> = Gap(from!!, to!!)

}
fun <W, P> gap(configuration: GapConfiguration<W, P>.()->Unit): Gap<W, P> = configure(configuration)