package org.drx.evoleq.dsl


class BlockConfiguration : Configuration<() -> Unit> {
    var block: ()->Unit = {}
    override fun configure(): () -> Unit {
        return block
    }
}

fun block(config: BlockConfiguration.()->Unit): BlockConfiguration {
    val b = BlockConfiguration()
    b.config()
    return b
}
class ExtensionBlockConfiguration<T> : Configuration<T.() -> Unit> {
    var block: T.()->Unit = {}
    override fun configure(): T.() -> Unit {
        return block
    }
}
fun <T> block(config: ExtensionBlockConfiguration<T>.()->Unit): ExtensionBlockConfiguration<T> {
    val b = ExtensionBlockConfiguration<T>()
    b.config()
    return b
}

class TerminalBlockConfiguration<T> : Configuration<(T) -> Unit> {
    var block:(T)->Unit = {}
    override fun configure(): (T) -> Unit {
        return block
    }
}

fun <T> termBlock(config: TerminalBlockConfiguration<T>.()->Unit): TerminalBlockConfiguration<T> {
    val b = TerminalBlockConfiguration<T>()
    b.config()
    return b
}