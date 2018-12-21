/**
 * Copyright (c) 2018 Dr. Florian Schmidt
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
package org.drx.evoleq.examples.application.message

import javafx.beans.property.SimpleObjectProperty
import org.drx.evoleq.examples.application.InitAppStub
import org.drx.evoleq.examples.application.Stub
import org.drx.evoleq.examples.application.dsl.ConfigurationEntry
import org.drx.evoleq.examples.application.dsl.SuspendedConfigurationEntry


open class Message
object EmptyMessage : Message()
data class NotSupported(val receivedMessage: Message) : Message()
object Wait : Message()
class WaitForPropertyMessage<D>(val property: SimpleObjectProperty<D> ) : Message()

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
data class RegisterConfiguration(val entry: SuspendedConfigurationEntry): ConfigRequestMessage()
class RegisterConfigurations(val entries: ArrayList<SuspendedConfigurationEntry>): ConfigRequestMessage()

sealed class ConfigResponseMessage : ConfigMessage()
object RegisteredConfigurations : ConfigResponseMessage()
object RegisteredConfiguration : ConfigResponseMessage()

sealed class StubMessage : Message()
data class DriveStub<out D>(val stub: Stub<out D>,val initialData: D) : StubMessage()
class DriveStubs(vararg stubs: DriveStub<*>)