package org.drx.evoleq.examples.advanced

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Pane
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.conditions.once
import org.drx.evoleq.dsl.BlockConfiguration
import org.drx.evoleq.dsl.spatulasConfiguration
import org.drx.evoleq.evolve
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.examples.application.ApplicationStub
import org.drx.evoleq.examples.application.dsl.ConfigurationEntry
import org.drx.evoleq.examples.application.dsl.appStubConfiguration
import org.drx.evoleq.examples.application.dsl.entry
import org.drx.evoleq.examples.application.dsl.fxApp
import org.drx.evoleq.examples.application.fx.FxApp
import org.drx.evoleq.examples.application.fx.FxApplicationLauncher
import org.drx.evoleq.examples.application.fx.FxStopFunction
import org.drx.evoleq.examples.application.message.*


fun main() {
    runBlocking {
        fxApp<Message> {
            registerConfigurations(
                entry {
                    key = String::class
                    config = spatulasConfiguration<Int> {
                        filler = { x: Int -> Immediate { x + 1 } }
                    }
                },
                ConfigurationEntry(Unit::class, spatulasConfiguration<Int> {
                    filler = { x: Int ->
                        Immediate {
                            delay(1_000)
                            x + 1
                        }
                    }
                })
            )
            stubConfig = appStubConfiguration {
                stub = { m: Message ->
                    when (m) {
                        is FxMessage -> when (m) {
                            is FxStart, is FxInit, is FxLaunch, is FxShowStage<*>, is FxCloseStage<*> -> Immediate { m }
                            is FxStop -> {
                                delay(1_000)
                                (configurations.registry[FxStopFunction::class] as BlockConfiguration).configure()()
                                Immediate { m }
                            }
                        }
                        else -> Immediate { m }
                    }
                }
            }
            fxInitBlock = {
                println("Initializing")
            }
            fxStopBlock = {
                //println("Exiting platform")
                Platform.exit()
            }
            fxStartBlock = {
                val pane = Pane()
                this.scene = Scene(pane, 400.0, 400.0)
                this.title = "Test title"
                this.show()
            }
            initBlock = { println("here i am") }
        }




    val s = FxApplicationLauncher(FxApp.STUB_PROPERTY)
        .launch<FxApp<Message>>()
    val stub = s.get() as ApplicationStub<Message>
    evolve(
        FxStop as Message,
        once()
        ){
            d -> Immediate{
                println("stub action")
            stub.stub(d).get()
        }}
    }
    System.exit(0)
}