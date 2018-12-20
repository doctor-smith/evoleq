package org.drx.evoleq.examples.application.fx

import javafx.scene.Scene
import javafx.scene.layout.Pane
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.examples.application.dsl.FxAppConfiguration
import org.drx.evoleq.examples.application.dsl.fxAppConfiguration
import org.drx.evoleq.conditions.once
import org.drx.evoleq.coroutines.suspended
import org.drx.evoleq.dsl.suspendedFlow
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.dsl.appStubConfiguration
import org.drx.evoleq.flow.SuspendedFlow
import org.drx.evoleq.gap.Gap
import org.drx.evoleq.math.then

fun <D> launchFxAppFlow(
    appConfig: FxAppConfiguration<D>
): SuspendedFlow<ApplicationStub<D>,Boolean> =
    suspendedFlow<ApplicationStub<D>,Boolean> {
        conditions = once()
        flow = suspended{ Immediate{ appConfig.configure() }
        }

    }.then(
        phi = suspendedFlow<ApplicationStub<D>,Boolean> {
            conditions = once()
            flow = {
                appStub ->  FxApplicationLauncher(FxApp.STUB_PROPERTY).launch<FxApp<D>>() as Evolving<ApplicationStub<D>>
            }
        },
        cond = once(),
        gap = Gap(
            from = { appStub ->  Immediate{ appStub }},
            to = {appStub -> { Immediate{ appStub } }}
        )
    )
/*
fun main() {
    val b = runBlocking {
        val f = launchFxAppFlow(fxAppConfiguration<Int> {
            fxStartBlock = {
                scene = Scene(Pane(), 200.0, 200.0)
                show()
            }
            stubConfig =
                    appStubConfiguration { stub = { x -> Immediate { delay(1000); x } } }

        }).evolve(InitAppStub()).get()
        val x = f.stub(1).get()
        if(x != 1){
            throw Exception()
        }
        System.exit(0)
    }
}
*/