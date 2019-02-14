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
package org.drx.evoleq.experimental

import org.drx.evoleq.dsl.GapConfiguration
import org.drx.evoleq.dsl.configure
import org.drx.evoleq.dsl.initialSideEffect
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.sideeffect.InitialSideEffect


class CatchingGapConfiguration<W, P> : GapConfiguration<W, Unit>() {

    private var sideEffect: InitialSideEffect<P>? = null
    private var iota: (W.(P)->W)? = null

    override fun configure(): Gap<W, Unit> {
        from{w -> Immediate{Unit} }
        to {
                w, _ -> Immediate{ w.(iota!!)(sideEffect!!()) }
        }

        return super.configure()
    }

    fun sideEffect(catch: ()->P) {
        this.sideEffect = initialSideEffect ( catch )
    }
    fun iota(embedding: W.(P)->W) {
        this.iota = embedding
    }

}
fun <W, P> catchingGap(configuration: CatchingGapConfiguration<W, P>.()->Unit): Gap<W, Unit> = configure(configuration)