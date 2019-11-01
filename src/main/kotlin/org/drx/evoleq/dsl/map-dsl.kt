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

fun<K,V> map(configuration: HashMapConfiguration<K,V>.()->Unit): HashMap<K,V> = configure(configuration)
open class HashMapConfiguration<K,V> : Configuration<HashMap<K, V>> {

    val map: HashMap<K,V> by lazy { HashMap<K,V>() }

    override fun configure(): HashMap<K, V> =  map

    infix fun K.to(value: V) {
        map[this] = value
    }

    fun putAll(from: HashMap<K,V>) {
        map.putAll(from)
    }
}

@Suppress("unused")
fun <T, K, V> HashMapConfiguration<K,V>.from(obj: T, properties: T.()->Unit) = obj.properties()