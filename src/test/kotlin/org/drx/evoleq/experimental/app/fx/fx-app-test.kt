package org.drx.evoleq.experimental.app.fx

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.Pane
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.drx.evoleq.dsl.spatulasConfiguration
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.experimental.dsl.ConfigurationEntry
import org.drx.evoleq.experimental.dsl.appStubConfiguration
import org.drx.evoleq.experimental.dsl.entry
import org.drx.evoleq.experimental.dsl.fxApp
import org.drx.evoleq.gap.Spatula
import org.junit.Test

class FxAppTest {
    @Test
    fun testDsl() {
        val app = fxApp<String> {
            registerConfigurations(
                entry{
                    key= String::class
                    config = spatulasConfiguration<Int> {
                        filler = {x:Int -> Immediate{x+1} }
                        }
                },
                ConfigurationEntry(Unit::class, spatulasConfiguration<Int> {
                    filler = {x:Int -> Immediate{x+1} }
                } )
            )
            stubConfig  = appStubConfiguration{
                stub = {s:String -> Immediate{s+s}
                }
            }
            fxInitBlock = {
                println("Initializing")
            }
            fxStopBlock = {
                println("Exiting platgorm")
                Platform.exit()
            }
            fxStartBlock = {
                val pane = Pane()
                scene = Scene(pane, 400.0,400.0)
                title = "Test title"
                show()
            }
        }


        runBlocking {
            val appClass = app::class

            val s = FxApplicationLauncher(app.stubProperty()).launch<FxApp<String>>()
            delay(1_000)
        }
    }
}