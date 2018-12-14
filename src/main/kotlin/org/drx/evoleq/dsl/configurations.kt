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