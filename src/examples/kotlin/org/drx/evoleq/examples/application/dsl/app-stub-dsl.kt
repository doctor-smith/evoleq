package org.drx.evoleq.examples.application.dsl

import org.drx.evoleq.dsl.Configuration
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.application.ApplicationStub

class ApplicationStubConfiguration<D> : Configuration<ApplicationStub<D>>{
    var stub:suspend (D)-> Evolving<D> = {d -> Immediate{d} }

    override fun configure(): ApplicationStub<D> = object: ApplicationStub<D> {
        override suspend fun stub(d: D): Evolving<D> = this@ApplicationStubConfiguration.stub(d)
    }
}

fun <D> appStubConfiguration(setup: ApplicationStubConfiguration<D>.()->Unit): ApplicationStubConfiguration<D> {
    val config = ApplicationStubConfiguration<D>()
    config.setup()
    return config
}