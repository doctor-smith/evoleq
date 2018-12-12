package org.drx.evoleq.experimental.dsl

import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.gap.Filling

class FillingConfiguration<P> : Configuration<Filling<P>> {

    var filler: (suspend (P)-> Evolving<P>)? = null

    override fun configure(): Filling<P> = Filling(filler!!)
}