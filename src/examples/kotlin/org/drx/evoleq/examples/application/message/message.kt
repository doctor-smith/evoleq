package org.drx.evoleq.examples.application.message

import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.ConfigurationEntry


open class Message

sealed class FxMessage : Message()
object FxInit : FxMessage()
object FxStart : FxMessage()
object FxStop : FxMessage()
object FxLaunch : FxMessage()
data class FxShowStage<out K>(val key: K) : FxMessage()

sealed class FxResponseMessage : Message()
object FxInitRespose : FxResponseMessage()
object FxStartRespose : FxResponseMessage()
object FxStopRespose : FxResponseMessage()
object FxLaunchRespose : FxResponseMessage()
data class FxShowStageResponse<out K, D>(val key: K, val stub: Stub<D> = InitAppStub()) : FxResponseMessage()



sealed class ConfigMessage : Message()
data class RegisterConfiguration(val entry: ConfigurationEntry): ConfigMessage()
class RegisterConfigurations(vararg entries: ConfigurationEntry): ConfigMessage()