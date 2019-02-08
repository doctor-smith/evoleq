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