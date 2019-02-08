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


/**
 * Fx block keys
 */
sealed class FxBlock
object FxInitBlock : FxBlock()
object FxStartBlock : FxBlock()
object FxStopBlock : FxBlock()
object FxInitFunction : FxBlock()
object FxStartFunction : FxBlock()
object FxStopFunction : FxBlock()
/**
 * Stage manager function keys
 */
sealed class StageManagerBlock
object ShowStageFunction : StageManagerBlock()
object CloseStageFunction : StageManagerBlock()
object RegisterStageFunction : StageManagerBlock()
object UnregisterStageFunction : StageManagerBlock()

/**
 * app block keys
 * they will be injected during
 * the configuration phase
 */
sealed class AppBlock
object StubBlock : AppBlock()
object InitBlock : AppBlock()

sealed class ConfigBlock
object RegisterConfigBlock : ConfigBlock()
object RegisterConfigsBlock : ConfigBlock()


