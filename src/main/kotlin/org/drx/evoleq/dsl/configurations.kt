/**
 * Copyright (C) 2018 Dr. Florian Schmidt
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

import kotlin.reflect.KClass

open class Configurations {
    val registry : HashMap<KClass<*>,Configuration<*>>  by lazy {HashMap<KClass<*>,Configuration<*>>()}

    inline fun <reified K,C : Configuration<*>> register(c: C ) {registry[K::class] = c}

    inline fun <reified K> get(): Configuration<*> = registry[K::class] as Configuration<*>

    inline fun <reified K> get(key: K): Configuration<*> = registry[K::class] as Configuration<*>
}
class SpatulasConfigurations : Configurations() {

}

open class SuspendedConfigurations {
    val registry : HashMap<KClass<*>,SuspendedConfiguration<*>>  by lazy {HashMap<KClass<*>,SuspendedConfiguration<*>>()}

    inline fun <reified K,C : SuspendedConfiguration<*>> register(c: C ) {registry[K::class] = c}

    inline fun <reified K> get(): SuspendedConfiguration<*> = registry[K::class] as SuspendedConfiguration<*>

    inline fun <reified K> get(key: K): SuspendedConfiguration<*> = registry[K::class] as SuspendedConfiguration<*>
}