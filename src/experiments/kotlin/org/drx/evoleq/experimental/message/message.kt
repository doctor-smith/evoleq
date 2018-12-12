package org.drx.evoleq.experimental.message


open class Message

sealed class FxMessage : Message()
object FxInit : FxMessage()
object FxStart : FxMessage()
object FxStop : FxMessage()
object FxLaunch : FxMessage()
data class FxShowStage<out K>(val key: K) : FxMessage()