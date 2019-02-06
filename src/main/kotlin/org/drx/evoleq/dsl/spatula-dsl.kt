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
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.gap.Spatula
import org.drx.evoleq.gap.Spatulas
import org.drx.evoleq.gap.fill

class SpatulaConfiguration<W,P> : Configuration<Spatula<W, P>> {

    var filler: ((P)-> Evolving<P>)? = null

    override fun configure(): Spatula<W, P> = object: Spatula<W,P> {
        override suspend fun fill(gap: Gap<W, P>): (W) -> Evolving<W> = gap.fill(filler!!)
    }

    fun filler(filler: (P)-> Evolving<P>) {
        this.filler = filler
    }
}
fun <W,P> spatula(configure: SpatulaConfiguration<W,P>.()->Unit): Spatula<W,P> = configure(configure)
fun <W,P> spatulaConfiguration(configure: SpatulaConfiguration<W,P>.()->Unit): SpatulaConfiguration<W,P> = setupConfiguration ( configure )


class SpatulasConfiguration<P> : Configuration<Spatulas<P>> {
    var filler: ((P)-> Evolving<P>)? = null

    override fun configure(): Spatulas<P> = object: Spatulas<P> {
        override suspend fun <W> spatula(w: W): Spatula<W, P> = object: Spatula<W,P> {
            override suspend fun fill(gap: Gap<W, P>): (W) -> Evolving<W> = gap.fill( filler!! )
        }

    }
}

fun <P> spatulas(configure: SpatulasConfiguration<P>.()->Unit): Spatulas<P> = configure(configure)
fun <P> spatulasConfiguration(configure: SpatulasConfiguration<P>.()->Unit): SpatulasConfiguration<P> =  setupConfiguration ( configure )