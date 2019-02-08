package org.drx.evoleq.dsl

import javafx.beans.property.SimpleObjectProperty
import org.drx.evoleq.evolving.Evolving
import org.drx.evoleq.evolving.Immediate
import org.drx.evoleq.stub.ParentStubKey
import org.drx.evoleq.stub.Stub
import org.drx.evoleq.time.WaitForProperty
import java.lang.Exception
import kotlin.reflect.KClass

class StubConfiguration<D> : Configuration<Stub<D>> {

    var evolve: suspend (D)-> Evolving<D> = {d -> Immediate{d} }

    private val stubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }

    private val parentalStubs: HashMap<KClass<*>, Stub<*>> by lazy { HashMap<KClass<*>, Stub<*>>() }

    private val accessingParentStubs: HashMap<KClass<*>, KClass<*>> by lazy { HashMap<KClass<*>, KClass<*>>() }

    private val stubProperty = SimpleObjectProperty<Stub<D>>()

    //suspend fun stub(): Evolving<Stub<D>> = WaitForProperty(stubProperty).toChange()



    override fun configure(): Stub<D> {
        val stub = object : Stub<D> {

            override suspend fun evolve(d: D): Evolving<D> = this@StubConfiguration.evolve(d)

            override val stubs: HashMap<KClass<*>, Stub<*>>
                get() = this@StubConfiguration.stubs
        }

        accessingParentStubs.entries.forEach { entry ->
            val child = stub.stubs[entry.key]
            val parentStub = parentalStubs[entry.value]
            if(child != null && parentStub != null) {
                child.stubs[ParentStubKey::class] = parentStub
            }
        }
        stubProperty.value = stub
        return stub
    }

    fun evolve( flow:suspend (D)-> Evolving<D> ) {
        this.evolve = flow
    }

    fun parent(): Stub<*> = stubs[ParentStubKey::class]!!

    fun child(key : KClass<*>): Stub<*> = when(key == ParentStubKey::class) {
        true -> throw(Exception("accessing the parent is not allowed via child-function"))
        else -> stubs[key]!!
    }

    fun child( key: KClass<*>, stub: Stub<*>, accessedParentStub: KClass<*>?) {
        stubs[key] = stub
        if(accessedParentStub != null) {
            accessingParentStubs[key] = accessedParentStub
        }
    }

    fun parentalStub(key: KClass<*>, stub: Stub<*>) {
        parentalStubs[key] = stub
    }

}

fun <D> stub(configuration: StubConfiguration<D>.()->Unit) : Stub<D> = configure(configuration)