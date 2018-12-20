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