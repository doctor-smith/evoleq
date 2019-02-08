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