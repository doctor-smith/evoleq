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

open class ArrayListConfiguration<T> : Configuration<ArrayList<T>> {

    private val list: ArrayList<T> by lazy { arrayListOf<T>() }

    override fun configure(): ArrayList<T> = list

    @Suppress("unused")
    fun ArrayListConfiguration<T>.item(t: T) = list.add(t)
}

fun <T> arrayList(configuration: ArrayListConfiguration<T>.()->Unit): ArrayList<T> = configure(configuration)