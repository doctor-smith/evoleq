package org.drx.evoleq.dsl

import org.junit.Test

class MapDslTest {
    @Test fun testFrom() {
        class Class(val prop1: Int)
        val c = Class(7)
        val map = map<String,Int>{
            "x" to 1
            from(c) {
                "number" to prop1
            }
        }

        val x = map["x"]!!
        val number = map["number"]!!
        assert(x == 1)
        assert(number == 7)
    }
}