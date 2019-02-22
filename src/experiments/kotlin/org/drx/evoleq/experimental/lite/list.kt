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
package org.drx.evoleq.experimental.lite

typealias Stack<T> = LiteList<T>

sealed class LiteList<T> {
    class Empty<T> : LiteList<T>()
    data class Item<T>(val item: T, val tail: LiteList<T> = Empty()) : LiteList<T>()
}

/**
 * Append an item to an lite list
 */
fun <T> LiteList<T>.append(item: T): LiteList<T> = when(this){
    is LiteList.Empty -> LiteList.Item(item)
    is LiteList.Item -> when(tail) {
        is LiteList.Empty -> {
            val listItem = LiteList.Item(item)
            LiteList.Item(this.item, listItem)
            listItem
        }
        else -> tail.append(item)
    }
}
/**
 * Revert a lite list
 */
tailrec fun <T> LiteList<T>.revert(reversed: LiteList<T> = LiteList.Empty()): LiteList<T> = when(this) {
    is LiteList.Empty -> reversed
    is LiteList.Item -> tail.revert( reversed = reversed.push(item) )
}

/**
 * Class representing a pop without information loss
 */
sealed class Pop<T> {
    class Empty<T> : Pop<T>()
    data class Info<T>(val item: T, val list: LiteList<T>) : Pop<T>()
}

/**
 *
 */
fun <T> LiteList<T>.push(item: T): LiteList<T> = LiteList.Item(item, this)

/**
 * Get first entry of a lite list without loosing information
 */
fun <T> LiteList<T>.pop(): Pop<T> = when(this) {
    is LiteList.Empty -> Pop.Empty()
    is LiteList.Item -> Pop.Info(item, tail)
}

/**
 * Revert the list-part of a Pop
 */
fun <T> Pop<T>.revert(): Pop<T> = when(this) {
    is Pop.Empty -> this
    is Pop.Info -> Pop.Info(item, list.revert())
}

/**
 * Pop the last entry of a lite list without loosing information
 */
fun<T> LiteList<T>.popLast(collected: Pop<T> = Pop.Empty()): Pop<T> = revert().pop().revert()