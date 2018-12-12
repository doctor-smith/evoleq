package org.drx.evoleq.experimental.message

import javafx.stage.Stage

open class Message

sealed class FxMessage : Message()
object FxInit : FxMessage()
object FxStart : FxMessage()
object FxStop : FxMessage()
object FxLaunch : FxMessage()
data class FxShowStage<K>(val key: K) : FxMessage()