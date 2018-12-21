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

class TerminalBiBlockConfiguration<S,T> : Configuration<(S,T) -> Unit> {
    var block:(S,T)->Unit = {_,_->}
    override fun configure(): (S,T) -> Unit {
        return block
    }
}



fun <T> termBlock(config: TerminalBlockConfiguration<T>.()->Unit): TerminalBlockConfiguration<T> {
    val b = TerminalBlockConfiguration<T>()
    b.config()
    return b
}
fun <S,T> termBlock(config: TerminalBiBlockConfiguration<S,T>.()->Unit): TerminalBiBlockConfiguration<S,T> {
    val b = TerminalBiBlockConfiguration<S,T>()
    b.config()
    return b
}

class FunctionConfiguration<S,T> : Configuration<(S)->T> {
    var block : ((S)->T)? = null
    override fun configure(): (S) -> T = block!!
}

fun <S,T> functionBlock(config: FunctionConfiguration<S,T>.()->Unit): FunctionConfiguration<S, T> {
    val b = FunctionConfiguration<S,T>()
    b.config()
    return b
}

class BiFunctionConfiguration<R,S,T> : Configuration<(R,S)->T> {
    var block : ((R,S)->T)? = null
    override fun configure(): (R,S) -> T = block!!
}

fun <R,S,T> functionBlock(config: BiFunctionConfiguration<R,S,T>.()->Unit): BiFunctionConfiguration<R,S, T> {
    val b = BiFunctionConfiguration<R,S,T>()
    b.config()
    return b
}