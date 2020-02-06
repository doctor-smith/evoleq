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
package org.drx.evoleq.dsl

import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import kotlinx.coroutines.delay
import org.drx.evoleq.coroutines.blockUntil
import org.drx.evoleq.coroutines.blockWhileEmpty
import org.drx.evoleq.util.and
import org.drx.evoleq.util.valueIsNull
import java.util.function.Predicate

open class ArrayListConfiguration<T> : Configuration<ArrayList<T>> {

    private val list: ArrayList<T> by lazy { arrayListOf<T>() }

    override fun configure(): ArrayList<T> = list

    @Suppress("unused")
    fun ArrayListConfiguration<T>.item(t: T) = list.add(t)
}

fun <T> arrayList(configuration: ArrayListConfiguration<T>.()->Unit): ArrayList<T> = configure(configuration)

suspend fun <T, O> ArrayList<T>.onNext(action: suspend (T)->O): O {
    while (isEmpty()) {
        delay(1)
    }
    val t = first()
    removeAt(0)
    return action(t)
}

open class SmartArrayList<T> : ArrayList<T>() {

    private val isEmptyPrivate = SimpleBooleanProperty(true)
    val isEmpty = SimpleBooleanProperty(true)
    init{
        isEmpty.bind(isEmptyPrivate)
    }

    override fun add(element: T): Boolean =with(super.add(element)) {
        isEmptyPrivate.value = false
        this
    }


    override fun add(index: Int, element: T) = with(super.add(index, element)) {
        isEmptyPrivate.value = false
    }

    override fun addAll(elements: Collection<T>): Boolean = with(super.addAll(elements)) {
        if(elements.isNotEmpty()) {
            isEmptyPrivate.value = false
        }
        this
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean = with(super.addAll(index, elements)) {
        if(elements.isNotEmpty()) {
            isEmptyPrivate.value = false
        }
        this
    }

    override fun remove(element: T): Boolean = with(super.remove(element)) {
        isEmptyPrivate.value = isEmpty()
        this
    }

    override fun removeAt(index: Int): T = with(super.removeAt(index)) {
        isEmptyPrivate.value = isEmpty()
        this
    }

    override fun removeAll(elements: Collection<T>): Boolean = with(removeAll(elements)){
        isEmptyPrivate.value = isEmpty()
        this
    }

    override fun removeIf(filter: Predicate<in T>): Boolean = with(super.removeIf(filter)) {
        isEmptyPrivate.value = isEmpty()
        this
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) = with(super.removeRange(fromIndex, toIndex)) {
        isEmptyPrivate.value = isEmpty()
        this
    }
}

fun <T> smartArrayListOf(vararg elements: T) : SmartArrayList<T> {
    val list = SmartArrayList<T>()
    list.addAll(elements)
    return list
}

inline fun <S, reified T> SmartArrayList<S>.map(f: (S)->T): SmartArrayList<T> = smartArrayListOf(
    *(this as ArrayList<S>).map(f).toTypedArray()
)

suspend fun <T, O> SmartArrayList<T>.onNext(action: suspend (T)->O): O {
        blockWhileEmpty()
        val t = first()
        removeAt(0)
        return action(t)
}

suspend fun <T, O> SmartArrayList<T>.onNext(cancel: Property<O>, action: suspend (T)->O): O {
    blockUntil(cancel.valueIsNull() and isEmpty){value -> !value}
    ifEmpty {
        return cancel.value
    }
    val t = first()
    removeAt(0)
    return action(t)
}

open class SmartArrayListConfiguration<T> : Configuration<SmartArrayList<T>> {

    private val list: SmartArrayList<T> by lazy { smartArrayListOf<T>() }

    override fun configure(): SmartArrayList<T> = list

    @Suppress("unused")
    fun SmartArrayListConfiguration<T>.item(t: T) = list.add(t)

    @Suppress("unused")
    suspend fun SmartArrayListConfiguration<T>.itemSuspended(t: T) = list.add(t)
}

fun <T> smartArrayList(configuration: SmartArrayListConfiguration<T>.()->Unit): SmartArrayList<T> = configure(configuration)

suspend fun <T> smartArrayList(configuration:suspend  SmartArrayListConfiguration<T>.()->Unit): SmartArrayList<T> = configureSuspended(configuration)