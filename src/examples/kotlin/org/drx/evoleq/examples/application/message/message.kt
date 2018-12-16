package org.drx.evoleq.examples.application.message

import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.ConfigurationEntry


open class Message
object EmptyMessage : Message()
data class NotSupported(val receivedMessage: Message) : Message()

sealed class FxMessage : Message()
sealed class FxRequestMessage : FxMessage()
object FxInit : FxRequestMessage()
object FxStart : FxRequestMessage()
object FxStop : FxRequestMessage()
object FxLaunch : FxRequestMessage()
data class FxShowStage<out K>(val key: K) : FxRequestMessage()
data class FxCloseStage<out K>(val key: K) : FxRequestMessage()

sealed class FxResponseMessage : FxMessage()
object FxInitResponse : FxResponseMessage()
object FxStartResponse : FxResponseMessage()
object FxStopResponse : FxResponseMessage()
object FxLaunchResponse : FxResponseMessage()
data class FxShowStageResponse<out K, D>(val key: K, val stub: Stub<D> = InitAppStub()) : FxResponseMessage()
data class FxCloseStageResponse<out K, D>(val key: K, val stub: Stub<D> = InitAppStub()) : FxResponseMessage()



sealed class ConfigMessage : Message()
sealed class ConfigRequestMessage : ConfigMessage()
data class RegisterConfiguration(val entry: ConfigurationEntry): ConfigRequestMessage()
class RegisterConfigurations(vararg entries: ConfigurationEntry): ConfigRequestMessage()

sealed class ConfigResponseMessage : ConfigMessage()


sealed class StubMessage : Message()
data class DriveStub<out D>(val stub: Stub<out D>,val initialData: D) : StubMessage()
class DriveStubs(vararg stubs: DriveStub<*>)