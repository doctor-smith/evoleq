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


