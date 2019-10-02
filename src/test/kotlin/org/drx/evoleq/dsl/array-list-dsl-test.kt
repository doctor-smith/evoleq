package org.drx.evoleq.dsl

import org.junit.Test

class ArrayListDslTest {

    @Test fun basics() {
        val list = arrayList<String> {
            item("one")
            item("two")
            item("three")
        }

        assert(list.size == 3)
    }

}