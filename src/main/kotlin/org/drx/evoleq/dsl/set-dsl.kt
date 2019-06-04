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

open class HashSetConfiguration<T>: Configuration<HashSet<T>> {

    private val set: HashSet<T> by lazy { hashSetOf<T>() }

    override fun configure(): HashSet<T> = set

    @Suppress("unused")
    fun HashSetConfiguration<T>.item(t: T) = set.add(t)

}