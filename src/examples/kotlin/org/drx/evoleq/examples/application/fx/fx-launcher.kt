package org.drx.evoleq.examples.application.fx

import javafx.application.Application
import javafx.beans.property.SimpleObjectProperty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.time.WaitForProperty


class FxApplicationLauncher<Stub>(val stubProperty: SimpleObjectProperty<Stub> ) {
    suspend inline fun <reified C: Application> launch(): Evolving<Stub> {

        GlobalScope.launch {
            coroutineScope{
                Application.launch(C::class.java)
            }
        }
        return WaitForProperty(stubProperty).toChange()
    }
}
