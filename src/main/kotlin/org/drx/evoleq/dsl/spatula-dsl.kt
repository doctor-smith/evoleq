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